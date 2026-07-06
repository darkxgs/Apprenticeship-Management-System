-- =============================================================
-- إعادة حساب حالة كل الطلاب (ناجح / راسب / دور ثاني)
-- بنفس قواعد البرنامج تماماً، مع الجمع الصحيح للمواد المركّبة (70/30).
-- بيصلّح الحالات اللي اتخزنت غلط قبل إصلاح الحساب المزدوج.
-- الحالات الخاصة (غائب/محروم/مفصول/معتذر/مؤجل) بتفضل زي ما هي.
-- =============================================================
SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE OFF

DECLARE
  v_status       VARCHAR2(50);
  v_special      VARCHAR2(100);
  v_obtained     NUMBER;
  v_has_children NUMBER;
  v_fail_theory  NUMBER;
  v_fail_pa      NUMBER;   -- عملي أو تطبيقي
  v_subj_count   NUMBER;
  v_grade_count  NUMBER;
  v_updated      NUMBER := 0;
  v_skipped      NUMBER := 0;
BEGIN
  FOR st IN (
    SELECT id, profession, status
    FROM students
    WHERE status IS NULL
       OR TRIM(status) IS NULL
       OR status IN ('غير محدد', 'ناجح', 'راسب', 'دور ثاني')
  ) LOOP

    -- لازم يكون فيه مواد للمهنة ودرجات للطالب
    SELECT COUNT(*) INTO v_subj_count
      FROM subjects WHERE profession = st.profession;
    SELECT COUNT(*) INTO v_grade_count
      FROM student_grades WHERE student_id = st.id;

    IF v_subj_count = 0 OR v_grade_count = 0 THEN
      v_skipped := v_skipped + 1;
      CONTINUE;
    END IF;

    -- (1) تجاوز الحالات الخاصة: أي درجة سالبة مطابقة لكود حالة
    BEGIN
      SELECT ss.status_name INTO v_special
      FROM student_grades sg
      JOIN student_statuses ss ON ss.status_code = sg.obtained_mark
      WHERE sg.student_id = st.id
        AND sg.obtained_mark < 0
        AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      v_special := NULL;
    END;

    IF v_special IS NOT NULL THEN
      v_status := v_special;
    ELSE
      v_fail_theory := 0;
      v_fail_pa     := 0;

      -- المواد الرئيسية فقط (اللي مالهاش أب)
      FOR subj IN (
        SELECT id, name, type, pass_mark
        FROM subjects
        WHERE profession = st.profession
          AND parent_subject_id IS NULL
      ) LOOP

        SELECT COUNT(*) INTO v_has_children
          FROM subjects WHERE parent_subject_id = subj.id;

        IF v_has_children > 0 THEN
          -- مادة مركّبة: مجموع الأبناء (مرة واحدة)
          SELECT NVL(SUM(obtained_mark), 0) INTO v_obtained
            FROM student_grades
           WHERE student_id = st.id
             AND subject_id IN (SELECT id FROM subjects WHERE parent_subject_id = subj.id);
        ELSE
          -- مادة عادية: درجتها المباشرة
          SELECT NVL(MAX(obtained_mark), 0) INTO v_obtained
            FROM student_grades
           WHERE student_id = st.id AND subject_id = subj.id;
        END IF;

        IF v_obtained < subj.pass_mark THEN
          IF TRIM(subj.type) = 'تطبيقي'
             OR INSTR(NVL(subj.name, ' '), 'تطبيقي') > 0
             OR TRIM(subj.type) = 'عملي'
             OR TRIM(subj.name) = 'عملي' THEN
            v_fail_pa := v_fail_pa + 1;
          ELSE
            v_fail_theory := v_fail_theory + 1;
          END IF;
        END IF;
      END LOOP;

      IF v_fail_pa > 0 THEN
        v_status := 'راسب';
      ELSIF v_fail_theory >= 3 THEN
        v_status := 'راسب';
      ELSIF v_fail_theory >= 1 THEN
        v_status := 'دور ثاني';
      ELSE
        v_status := 'ناجح';
      END IF;
    END IF;

    -- حدّث فقط لو الحالة اتغيرت
    IF v_status IS NOT NULL AND (st.status IS NULL OR st.status <> v_status) THEN
      UPDATE students SET status = v_status WHERE id = st.id;
      v_updated := v_updated + 1;
    END IF;

  END LOOP;

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('=====================================');
  DBMS_OUTPUT.PUT_LINE('تم تحديث حالة ' || v_updated || ' طالب.');
  DBMS_OUTPUT.PUT_LINE('تم تخطي ' || v_skipped || ' طالب (بدون درجات/مواد).');
  DBMS_OUTPUT.PUT_LINE('=====================================');
END;
/

EXIT
