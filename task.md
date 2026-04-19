# Task Tracker — 7 Phases

## Phase 1: إعادة ترتيب الفلاتر
- [x] StudentsPage.java — reorder filter bar (المنطقة ← المركز ← التخصص ← الحالة)
- [x] DataEntryPage.java — same reorder

## Phase 2: تطوير المواد الدراسية  
- [x] Subject.java — add parentSubjectId, subName fields
- [x] DatabaseConnection.java — add parent_subject_id, sub_name columns
- [x] SubjectService.java — new autoGenerate order, LIMIT validation, composite support
- [x] SubjectsPage.java — composite card UI, limit validation, 30/70 toggle
- [x] DataEntryPage.java — composite subject rendering

## Phase 3: إعادة هيكلة الـ Sidebar
- [x] Sidebar.java — workflow-based sections with JSeparators
- [x] SecretNumberPage.java — NEW page for secret number management

## Phase 4: Validation للبيانات
- [x] ValidationService.java — NEW: national ID parsing, phone validation
- [x] ExcelService.java — validation implemented (runs in main thread)
- [x] StudentFormPage.java — auto-fill from national ID

## Phase 5: إزالة الصفر البادئ
- [x] SystemSettingsPage.java — strip leading zeros from codes + unique code validation

## Phase 6: خوارزمية الرقم السري
- [x] SecretNumberService.java — NEW: generation algorithm
- [x] SecretNumberPage.java — review/generate/import/export UI

## Phase 7: تحسين Logging
- [x] LogService.java — thread-safe, detailed logging
- [x] Add log calls to StudentService, ExcelService, SubjectService
- [x] AdminLogsPage.java — add filters (search + action filter)
