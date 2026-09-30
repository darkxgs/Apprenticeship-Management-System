package com.pvtd.students.services;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pvtd.students.db.DatabaseConnection;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * نقل كامل لكل بيانات البرنامج من جهاز لجهاز.
 *
 * التصدير يكتب ملفاً واحداً (.pvtd وهو ZIP) فيه كل الجداول صفاً صفاً بنفس
 * الأرقام الداخلية (id) + صور الطلاب والبطاقات. الاستيراد يمسح بيانات الجهاز
 * الحالي ويضع مكانها نسخة طبق الأصل — الطلاب، الدرجات، المواد المركبة (30/70)،
 * الحالات، الأرقام السرية، المستخدمين، الإعدادات، السجل، والأرشيف.
 *
 * قبل الاستيراد تُحفظ نسخة كاملة من بيانات الجهاز الحالي تلقائياً، وكل
 * الإدخال يتم في معاملة واحدة: إما ينجح كله أو لا يتغير شيء.
 */
public class DataTransferService {

    public static final String EXTENSION = "pvtd";
    private static final int FORMAT_VERSION = 1;
    private static final String MAGIC = "PVTD-FULL-TRANSFER";

    /** كل جداول البرنامج بترتيب يسبق فيه الأب الابن */
    private static final String[] TABLES = {
            "DEPARTMENTS", "SPECIALIZATIONS", "REGIONS", "CENTERS",
            "PROFESSIONAL_GROUPS", "PROFESSIONS", "SUBJECTS", "STUDENTS", "STUDENT_GRADES",
            "USERS", "STUDENT_STATUSES", "SYSTEM_DICTIONARIES", "SYSTEM_SETTINGS", "LOGS",
            "ARCHIVE_GROUPS", "ARCHIVED_STUDENTS"
    };

    /** أعمدة جدول الطلاب التي تحمل مسارات ملفات صور */
    private static final String[] PATH_COLUMNS = { "IMAGE_PATH", "ID_FRONT_PATH", "ID_BACK_PATH" };

    private static final int BATCH = 500;

    public interface Progress {
        void update(String status, int percent);
    }

    public static class Summary {
        public final LinkedHashMap<String, Long> rows = new LinkedHashMap<>();
        public int images;
        public int missingImages;
        public final List<String> warnings = new ArrayList<>();
        public File backupFile;
        public String createdAt;
    }

    // ═══════════════════════════════ Export ═══════════════════════════════

    public static Summary exportAll(File out, Progress p) throws Exception {
        Progress progress = p != null ? p : (s, v) -> { };
        Summary sum = new Summary();
        File tmp = new File(out.getAbsoluteFile().getParentFile(), out.getName() + ".tmp");

        Map<String, String> imageEntries = new LinkedHashMap<>(); // المسار الأصلي -> مكانه داخل الملف
        Map<String, String> idSums = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)),
                     StandardCharsets.UTF_8)) {

            Set<String> existing = existingTables(conn);
            List<String> tables = new ArrayList<>();
            for (String t : TABLES) if (existing.contains(t)) tables.add(t);

            Map<String, Long> counts = new HashMap<>();
            long total = 0;
            for (String t : tables) {
                long c = count(conn, t);
                counts.put(t, c);
                total += c;
            }

            Set<String> imagePaths = new LinkedHashSet<>();
            long done = 0;
            for (String t : tables) {
                progress.update("تصدير: " + arabicName(t), percent(done, total, 0, 80));
                zip.putNextEntry(new ZipEntry("data/" + t + ".json"));
                JsonWriter w = new JsonWriter(new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8)));
                long rowsWritten = 0;
                BigDecimal idSum = BigDecimal.ZERO;

                try (Statement st = conn.createStatement()) {
                    st.setFetchSize(1000);
                    boolean hasId = columnTypes(conn, t).containsKey("ID");
                    try (ResultSet rs = st.executeQuery("SELECT * FROM " + t + (hasId ? " ORDER BY ID" : ""))) {
                        ResultSetMetaData md = rs.getMetaData();
                        int n = md.getColumnCount();
                        char[] kinds = new char[n + 1];
                        int idIndex = -1;
                        int[] pathIdx = new int[n + 1];

                        w.beginObject();
                        w.name("table").value(t);
                        w.name("columns").beginArray();
                        for (int i = 1; i <= n; i++) {
                            String name = md.getColumnName(i).toUpperCase(Locale.ROOT);
                            kinds[i] = kindOf(md.getColumnType(i));
                            if (name.equals("ID")) idIndex = i;
                            if (t.equals("STUDENTS") && Arrays.asList(PATH_COLUMNS).contains(name)) pathIdx[i] = 1;
                            w.beginObject().name("name").value(name).name("kind").value(String.valueOf(kinds[i])).endObject();
                        }
                        w.endArray();

                        w.name("rows").beginArray();
                        while (rs.next()) {
                            w.beginArray();
                            for (int i = 1; i <= n; i++) {
                                String v = readValue(rs, i, kinds[i]);
                                if (v == null) w.nullValue(); else w.value(v);
                                if (i == idIndex && v != null) idSum = idSum.add(new BigDecimal(v));
                                if (pathIdx[i] == 1 && v != null && !v.trim().isEmpty()) imagePaths.add(v);
                            }
                            w.endArray();
                            rowsWritten++;
                            done++;
                            if (rowsWritten % 2000 == 0) {
                                progress.update("تصدير: " + arabicName(t) + " (" + rowsWritten + ")", percent(done, total, 0, 80));
                            }
                        }
                        w.endArray();
                        w.endObject();
                        if (idIndex > 0) idSums.put(t, idSum.toPlainString());
                    }
                }
                w.flush();          // لا نغلق الكاتب حتى لا يُغلق ملف الـ ZIP نفسه
                zip.closeEntry();
                sum.rows.put(t, rowsWritten);
            }

            // ── الصور ──
            int i = 0;
            for (String path : imagePaths) {
                i++;
                File f = new File(path);
                if (!f.isFile()) {
                    sum.missingImages++;
                    continue;
                }
                String entry = "images/" + i + "/" + safeFileName(f.getName());
                zip.putNextEntry(new ZipEntry(entry));
                Files.copy(f.toPath(), zip);
                zip.closeEntry();
                imageEntries.put(path, entry);
                sum.images++;
                if (i % 50 == 0) progress.update("نسخ الصور (" + i + " / " + imagePaths.size() + ")",
                        percent(i, imagePaths.size(), 80, 99));
            }

            // ── ملف الوصف (يُكتب في النهاية لأنه يحتاج الأعداد الفعلية) ──
            sum.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
            zip.putNextEntry(new ZipEntry("manifest.json"));
            JsonWriter mw = new JsonWriter(new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8)));
            mw.setIndent("  ");
            mw.beginObject();
            mw.name("magic").value(MAGIC);
            mw.name("formatVersion").value(FORMAT_VERSION);
            mw.name("createdAt").value(sum.createdAt);
            mw.name("computer").value(computerName());
            mw.name("tables").beginObject();
            for (Map.Entry<String, Long> e : sum.rows.entrySet()) mw.name(e.getKey()).value(e.getValue());
            mw.endObject();
            mw.name("idSums").beginObject();
            for (Map.Entry<String, String> e : idSums.entrySet()) mw.name(e.getKey()).value(e.getValue());
            mw.endObject();
            mw.name("images").beginObject();
            for (Map.Entry<String, String> e : imageEntries.entrySet()) mw.name(e.getKey()).value(e.getValue());
            mw.endObject();
            mw.endObject();
            mw.flush();
            zip.closeEntry();
        } catch (Exception e) {
            tmp.delete();
            throw e;
        }

        Files.move(tmp.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        progress.update("تم التصدير", 100);
        return sum;
    }

    // ═══════════════════════════════ Import ═══════════════════════════════

    /** قراءة وصف الملف فقط (لعرضه على المستخدم قبل التأكيد) */
    public static Summary readInfo(File in) throws Exception {
        try (ZipFile zf = openZip(in)) {
            Manifest m = readManifest(zf);
            Summary s = new Summary();
            s.rows.putAll(m.tables);
            s.images = m.images.size();
            s.createdAt = m.createdAt + (m.computer.isEmpty() ? "" : "  (جهاز: " + m.computer + ")");
            return s;
        }
    }

    public static Summary importAll(File in, File backupDir, Progress p) throws Exception {
        Progress progress = p != null ? p : (s, v) -> { };
        Summary sum = new Summary();

        try (ZipFile zf = openZip(in)) {
            Manifest m = readManifest(zf);

            // 1) نسخة أمان من بيانات هذا الجهاز قبل أي تغيير
            backupDir.mkdirs();
            String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
            File backup = new File(backupDir, "قبل_الاستيراد_" + stamp + "." + EXTENSION);
            exportAll(backup, (s, v) -> progress.update("حفظ نسخة أمان من بيانات هذا الجهاز: " + s, v * 25 / 100));
            sum.backupFile = backup;

            // 2) تجهيز الجداول (جداول الأرشيف قد لا تكون أنشئت بعد على الجهاز الجديد)
            progress.update("تجهيز قاعدة البيانات...", 26);
            ArchiveService.ensureTables();

            // 3) فك الصور في مجلد جديد وتجهيز خريطة المسارات الجديدة
            File imagesRoot = new File("students_images", "imported_" + stamp).getAbsoluteFile();
            Map<String, String> newPaths = new HashMap<>();
            int k = 0;
            for (Map.Entry<String, String> e : m.images.entrySet()) {
                ZipEntry ze = zf.getEntry(e.getValue());
                if (ze == null) continue;
                File dest = new File(imagesRoot, e.getValue().substring("images/".length()));
                dest.getParentFile().mkdirs();
                try (InputStream is = zf.getInputStream(ze)) {
                    Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                newPaths.put(e.getKey(), dest.getAbsolutePath());
                if (++k % 50 == 0) progress.update("نسخ الصور (" + k + " / " + m.images.size() + ")",
                        percent(k, m.images.size(), 27, 35));
            }
            sum.images = newPaths.size();

            try {
                importTables(zf, m, newPaths, sum, progress);
            } catch (Exception ex) {
                deleteTree(imagesRoot);
                throw ex;
            }
        }
        progress.update("تم الاستيراد", 100);
        return sum;
    }

    private static void importTables(ZipFile zf, Manifest m, Map<String, String> newPaths,
                                     Summary sum, Progress progress) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Set<String> existing = existingTables(conn);

            List<String> tables = new ArrayList<>();
            for (String t : fkOrder(conn, Arrays.asList(TABLES))) {
                if (!m.tables.containsKey(t)) continue;
                if (!existing.contains(t)) {
                    sum.warnings.add("الجدول " + arabicName(t) + " غير موجود على هذا الجهاز فلم يُنقل.");
                    continue;
                }
                tables.add(t);
            }

            // المُشغّلات (triggers) يجب ألا تغيّر الأرقام الداخلية التي ننقلها
            // (DDL في أوراكل يحفظ تلقائياً، لذا يتم قبل بدء المعاملة)
            progress.update("تجهيز قاعدة البيانات...", 36);
            ensureIdPreservingTriggers(conn, tables);

            long total = 0;
            for (String t : tables) total += m.tables.get(t);

            conn.setAutoCommit(false);
            try {
                // مسح البيانات الحالية: الأبناء أولاً
                for (int i = tables.size() - 1; i >= 0; i--) {
                    progress.update("مسح البيانات القديمة: " + arabicName(tables.get(i)), 37);
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate("DELETE FROM " + tables.get(i));
                    }
                }

                long[] done = { 0 };
                for (String t : tables) {
                    long n = insertTable(conn, zf, t, newPaths, sum, progress, done, total);
                    sum.rows.put(t, n);
                }

                // التحقق: نفس عدد الصفوف ونفس الأرقام الداخلية بالضبط
                progress.update("التحقق من البيانات...", 96);
                for (String t : tables) {
                    long expected = m.tables.get(t);
                    long actual = count(conn, t);
                    if (actual != expected) {
                        throw new SQLException("عدد صفوف " + arabicName(t) + " بعد النقل (" + actual
                                + ") لا يساوي الملف (" + expected + ")");
                    }
                    String expectedSum = m.idSums.get(t);
                    if (expectedSum != null) {
                        String actualSum = idSum(conn, t);
                        if (new BigDecimal(actualSum).compareTo(new BigDecimal(expectedSum)) != 0) {
                            throw new SQLException("الأرقام الداخلية لجدول " + arabicName(t)
                                    + " تغيّرت أثناء النقل (يوجد trigger على الجدول يغيّر id).");
                        }
                    }
                }

                conn.commit();
            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignored) { }
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (Exception ignored) { }
            }

            // الـ sequences تبدأ بعد أكبر رقم منقول حتى لا تتكرر الأرقام عند الإضافة الجديدة
            progress.update("ضبط العدادات...", 98);
            for (String t : tables) {
                try {
                    advanceSequence(conn, t);
                } catch (SQLException ex) {
                    sum.warnings.add("تعذر ضبط عداد " + arabicName(t) + ": " + ex.getMessage());
                }
            }
        }
    }

    private static long insertTable(Connection conn, ZipFile zf, String table, Map<String, String> newPaths,
                                    Summary sum, Progress progress, long[] done, long total) throws Exception {
        ZipEntry ze = zf.getEntry("data/" + table + ".json");
        if (ze == null) throw new IOException("الملف ناقص: بيانات " + arabicName(table) + " غير موجودة");

        Map<String, Integer> targetTypes = columnTypes(conn, table);
        long inserted = 0;

        try (JsonReader r = new JsonReader(new BufferedReader(
                new InputStreamReader(zf.getInputStream(ze), StandardCharsets.UTF_8)))) {
            List<String> names = new ArrayList<>();
            List<Character> kinds = new ArrayList<>();

            r.beginObject();
            while (r.hasNext()) {
                String key = r.nextName();
                if (key.equals("columns")) {
                    r.beginArray();
                    while (r.hasNext()) {
                        String name = null, kind = "S";
                        r.beginObject();
                        while (r.hasNext()) {
                            String ck = r.nextName();
                            if (ck.equals("name")) name = r.nextString();
                            else if (ck.equals("kind")) kind = r.nextString();
                            else r.skipValue();
                        }
                        r.endObject();
                        names.add(name.toUpperCase(Locale.ROOT));
                        kinds.add(kind.isEmpty() ? 'S' : kind.charAt(0));
                    }
                    r.endArray();
                } else if (key.equals("rows")) {
                    // الأعمدة المشتركة فقط بين الملف وهذا الجهاز
                    List<Integer> use = new ArrayList<>();
                    StringBuilder cols = new StringBuilder(), marks = new StringBuilder();
                    for (int i = 0; i < names.size(); i++) {
                        if (!targetTypes.containsKey(names.get(i))) {
                            sum.warnings.add("العمود " + names.get(i) + " في " + arabicName(table)
                                    + " غير موجود على هذا الجهاز فلم يُنقل.");
                            continue;
                        }
                        use.add(i);
                        if (cols.length() > 0) { cols.append(", "); marks.append(", "); }
                        cols.append('"').append(names.get(i)).append('"');
                        marks.append('?');
                    }
                    if (use.isEmpty()) throw new SQLException("لا توجد أعمدة مشتركة لجدول " + arabicName(table));

                    boolean isStudents = table.equals("STUDENTS");
                    Set<String> pathCols = new HashSet<>(Arrays.asList(PATH_COLUMNS));
                    String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ")";

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        int pending = 0;
                        String[] row = new String[names.size()];
                        r.beginArray();
                        while (r.hasNext()) {
                            Arrays.fill(row, null);
                            r.beginArray();
                            int c = 0;
                            while (r.hasNext()) {
                                String v;
                                if (r.peek() == JsonToken.NULL) { r.nextNull(); v = null; }
                                else v = r.nextString();
                                if (c < row.length) row[c] = v;
                                c++;
                            }
                            r.endArray();

                            for (int j = 0; j < use.size(); j++) {
                                int i = use.get(j);
                                String v = row[i];
                                String name = names.get(i);
                                if (isStudents && v != null && pathCols.contains(name) && newPaths.containsKey(v)) {
                                    v = newPaths.get(v);
                                }
                                bind(ps, j + 1, v, kinds.get(i), targetTypes.get(name));
                            }
                            ps.addBatch();
                            inserted++;
                            done[0]++;
                            if (++pending >= BATCH) {
                                ps.executeBatch();
                                pending = 0;
                                progress.update("استيراد: " + arabicName(table) + " (" + inserted + ")",
                                        percent(done[0], total, 38, 95));
                            }
                        }
                        r.endArray();
                        if (pending > 0) ps.executeBatch();
                    }
                } else {
                    r.skipValue();
                }
            }
            r.endObject();
        }
        progress.update("استيراد: " + arabicName(table), percent(done[0], total, 38, 95));
        return inserted;
    }

    // ═══════════════════════════════ Helpers ═══════════════════════════════

    private static class Manifest {
        String createdAt = "";
        String computer = "";
        final LinkedHashMap<String, Long> tables = new LinkedHashMap<>();
        final Map<String, String> idSums = new HashMap<>();
        final LinkedHashMap<String, String> images = new LinkedHashMap<>();
    }

    private static ZipFile openZip(File in) throws IOException {
        try {
            return new ZipFile(in, StandardCharsets.UTF_8);
        } catch (java.util.zip.ZipException e) {
            throw new IOException("هذا الملف ليس ملف نقل بيانات من البرنامج، أو أنه تالف (لم يكتمل نسخه).", e);
        }
    }

    private static Manifest readManifest(ZipFile zf) throws IOException {
        ZipEntry ze = zf.getEntry("manifest.json");
        if (ze == null) throw new IOException("هذا الملف ليس ملف نقل بيانات من البرنامج.");
        Manifest m = new Manifest();
        String magic = null;
        int version = -1;
        try (JsonReader r = new JsonReader(new InputStreamReader(zf.getInputStream(ze), StandardCharsets.UTF_8))) {
            r.beginObject();
            while (r.hasNext()) {
                String k = r.nextName();
                switch (k) {
                    case "magic": magic = r.nextString(); break;
                    case "formatVersion": version = r.nextInt(); break;
                    case "createdAt": m.createdAt = r.nextString(); break;
                    case "computer": m.computer = r.nextString(); break;
                    case "tables":
                        r.beginObject();
                        while (r.hasNext()) m.tables.put(r.nextName(), r.nextLong());
                        r.endObject();
                        break;
                    case "idSums":
                        r.beginObject();
                        while (r.hasNext()) m.idSums.put(r.nextName(), r.nextString());
                        r.endObject();
                        break;
                    case "images":
                        r.beginObject();
                        while (r.hasNext()) m.images.put(r.nextName(), r.nextString());
                        r.endObject();
                        break;
                    default: r.skipValue();
                }
            }
            r.endObject();
        }
        if (!MAGIC.equals(magic)) throw new IOException("هذا الملف ليس ملف نقل بيانات من البرنامج.");
        if (version > FORMAT_VERSION) {
            throw new IOException("الملف مصدَّر من نسخة أحدث من البرنامج. حدّث البرنامج على هذا الجهاز أولاً.");
        }
        return m;
    }

    /** N رقم، T تاريخ/وقت، B بيانات ثنائية، S نص */
    private static char kindOf(int sqlType) {
        switch (sqlType) {
            case Types.NUMERIC: case Types.DECIMAL: case Types.INTEGER: case Types.SMALLINT:
            case Types.TINYINT: case Types.BIGINT: case Types.FLOAT: case Types.DOUBLE: case Types.REAL:
                return 'N';
            case Types.DATE: case Types.TIMESTAMP: case Types.TIME:
            case Types.TIMESTAMP_WITH_TIMEZONE: case -101: case -102:
                return 'T';
            case Types.BLOB: case Types.BINARY: case Types.VARBINARY: case Types.LONGVARBINARY:
                return 'B';
            default:
                return 'S';
        }
    }

    private static String readValue(ResultSet rs, int i, char kind) throws SQLException {
        switch (kind) {
            case 'N': {
                BigDecimal b = rs.getBigDecimal(i);
                return b == null ? null : b.toPlainString();
            }
            case 'T': {
                Timestamp ts = rs.getTimestamp(i);
                return ts == null ? null : String.valueOf(ts.getTime());
            }
            case 'B': {
                byte[] b = rs.getBytes(i);
                return b == null ? null : Base64.getEncoder().encodeToString(b);
            }
            default:
                return rs.getString(i);
        }
    }

    private static void bind(PreparedStatement ps, int idx, String v, char kind, int targetType) throws SQLException {
        if (v == null) {
            ps.setNull(idx, targetType);
            return;
        }
        switch (kind) {
            case 'N': ps.setBigDecimal(idx, new BigDecimal(v)); break;
            case 'T': ps.setTimestamp(idx, new Timestamp(Long.parseLong(v))); break;
            case 'B': ps.setBytes(idx, Base64.getDecoder().decode(v)); break;
            default: ps.setString(idx, v);
        }
    }

    private static Set<String> existingTables(Connection conn) throws SQLException {
        Set<String> s = new HashSet<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT table_name FROM user_tables")) {
            while (rs.next()) s.add(rs.getString(1).toUpperCase(Locale.ROOT));
        }
        return s;
    }

    private static Map<String, Integer> columnTypes(Connection conn, String table) throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " WHERE 1=0")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                m.put(md.getColumnName(i).toUpperCase(Locale.ROOT), md.getColumnType(i));
            }
        }
        return m;
    }

    private static long count(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String idSum(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT NVL(SUM(id), 0) FROM " + table)) {
            rs.next();
            return rs.getBigDecimal(1).toPlainString();
        }
    }

    /** ترتيب الجداول بحيث يأتي كل جدول بعد الجداول التي يشير إليها (حسب قيود قاعدة البيانات الفعلية) */
    private static List<String> fkOrder(Connection conn, List<String> tables) {
        Map<String, Set<String>> parents = new HashMap<>();
        for (String t : tables) parents.put(t, new HashSet<>());
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT a.table_name, c.table_name FROM user_constraints a " +
                     "JOIN user_constraints c ON a.r_constraint_name = c.constraint_name " +
                     "WHERE a.constraint_type = 'R'")) {
            while (rs.next()) {
                String child = rs.getString(1).toUpperCase(Locale.ROOT);
                String parent = rs.getString(2).toUpperCase(Locale.ROOT);
                if (parents.containsKey(child) && parents.containsKey(parent) && !child.equals(parent)) {
                    parents.get(child).add(parent);
                }
            }
        } catch (SQLException e) {
            return tables; // الترتيب الافتراضي صحيح أصلاً
        }
        List<String> ordered = new ArrayList<>();
        Set<String> placed = new HashSet<>();
        boolean progress = true;
        while (ordered.size() < tables.size() && progress) {
            progress = false;
            for (String t : tables) {
                if (!placed.contains(t) && placed.containsAll(parents.get(t))) {
                    ordered.add(t);
                    placed.add(t);
                    progress = true;
                }
            }
        }
        for (String t : tables) if (!placed.contains(t)) ordered.add(t);
        return ordered;
    }

    /** إعادة إنشاء مُشغّلات الترقيم بحيث لا تستبدل id إلا لو كان فارغاً */
    private static void ensureIdPreservingTriggers(Connection conn, List<String> tables) throws SQLException {
        Set<String> triggers = new HashSet<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT trigger_name FROM user_triggers")) {
            while (rs.next()) triggers.add(rs.getString(1).toUpperCase(Locale.ROOT));
        }
        for (String t : tables) {
            String trg = "TRG_" + t + "_SEQ";
            if (!triggers.contains(trg)) continue;
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE OR REPLACE TRIGGER " + trg + " BEFORE INSERT ON " + t +
                        " FOR EACH ROW BEGIN IF :new.id IS NULL THEN :new.id := " + t + "_SEQ.nextval; END IF; END;");
            }
        }
    }

    /** تقديم الـ sequence إلى ما بعد أكبر id موجود */
    private static void advanceSequence(Connection conn, String table) throws SQLException {
        if (!columnTypes(conn, table).containsKey("ID")) return;
        String seq = table + "_SEQ";
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM user_sequences WHERE sequence_name = ?")) {
            ps.setString(1, seq);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return;
            }
        }
        long max;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT NVL(MAX(id), 0) FROM " + table)) {
            rs.next();
            max = rs.getLong(1);
        }
        long cur = nextVal(conn, seq);
        if (cur > max) return;
        long diff = max - cur + 1;
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER SEQUENCE " + seq + " INCREMENT BY " + diff);
            try {
                nextVal(conn, seq);
            } finally {
                st.execute("ALTER SEQUENCE " + seq + " INCREMENT BY 1");
            }
        }
    }

    private static long nextVal(Connection conn, String seq) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + seq + ".NEXTVAL FROM dual")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static int percent(long done, long total, int from, int to) {
        if (total <= 0) return to;
        return (int) (from + (to - from) * Math.min(done, total) / total);
    }

    private static String safeFileName(String name) {
        String s = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return s.isEmpty() ? "image" : s;
    }

    private static String computerName() {
        String n = System.getenv("COMPUTERNAME");
        return n != null ? n : "";
    }

    private static void deleteTree(File f) {
        if (f == null || !f.exists()) return;
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) deleteTree(k);
        f.delete();
    }

    public static String arabicName(String table) {
        switch (table.toUpperCase(Locale.ROOT)) {
            case "DEPARTMENTS": return "الأقسام";
            case "SPECIALIZATIONS": return "التخصصات";
            case "REGIONS": return "المناطق";
            case "CENTERS": return "المراكز";
            case "PROFESSIONAL_GROUPS": return "المجموعات المهنية";
            case "PROFESSIONS": return "المهن";
            case "SUBJECTS": return "المواد";
            case "STUDENTS": return "الطلاب";
            case "STUDENT_GRADES": return "الدرجات";
            case "USERS": return "المستخدمين";
            case "STUDENT_STATUSES": return "حالات الطلاب";
            case "SYSTEM_DICTIONARIES": return "القوائم";
            case "SYSTEM_SETTINGS": return "الإعدادات";
            case "LOGS": return "سجل العمليات";
            case "ARCHIVE_GROUPS": return "مجموعات الأرشيف";
            case "ARCHIVED_STUDENTS": return "الطلاب المؤرشفين";
            default: return table;
        }
    }
}
