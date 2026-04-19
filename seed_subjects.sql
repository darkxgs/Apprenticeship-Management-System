-- Subject seed: 4 subjects per profession, INSERT only if not exists
-- Uses (SELECT MIN(id) FROM specializations) for specialization_id

DEFINE ins = "INSERT INTO subjects(profession,name,type,pass_mark,max_mark,specialization_id,display_order) SELECT"
DEFINE chk = "FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM subjects WHERE profession="

-- البرادة العامة
&ins 'البرادة العامة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'البرادة العامة' AND name='لغة انجليزية');
&ins 'البرادة العامة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'البرادة العامة' AND name='ميكانيكا عامه');
&ins 'البرادة العامة','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'البرادة العامة' AND name='تكنولوجيا ومقايسات');
&ins 'البرادة العامة','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'البرادة العامة' AND name='رسم هندسى + CAD');
-- خراطة المعادن
&ins 'خراطة المعادن','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'خراطة المعادن' AND name='لغة انجليزية');
&ins 'خراطة المعادن','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'خراطة المعادن' AND name='ميكانيكا عامه');
&ins 'خراطة المعادن','تكنولوجيا وحساب فنى','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'خراطة المعادن' AND name='تكنولوجيا وحساب فنى');
&ins 'خراطة المعادن','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'خراطة المعادن' AND name='رسم هندسى + CAD');
-- لحام المعادن
&ins 'لحام المعادن','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'لحام المعادن' AND name='لغة انجليزية');
&ins 'لحام المعادن','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'لحام المعادن' AND name='ميكانيكا عامه');
&ins 'لحام المعادن','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'لحام المعادن' AND name='تكنولوجيا ومقايسات');
&ins 'لحام المعادن','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'لحام المعادن' AND name='رسم فنى');
-- تشغيل المخارط المبرمجة CNC
&ins 'تشغيل المخارط المبرمجة بالحاسب CNC','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشغيل المخارط المبرمجة بالحاسب CNC' AND name='لغة انجليزية');
&ins 'تشغيل المخارط المبرمجة بالحاسب CNC','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشغيل المخارط المبرمجة بالحاسب CNC' AND name='ميكانيكا عامه');
&ins 'تشغيل المخارط المبرمجة بالحاسب CNC','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشغيل المخارط المبرمجة بالحاسب CNC' AND name='تكنولوجيا ومقايسات');
&ins 'تشغيل المخارط المبرمجة بالحاسب CNC','التصميم والتصنيع بالحاسب CAD/CAM','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشغيل المخارط المبرمجة بالحاسب CNC' AND name='التصميم والتصنيع بالحاسب CAD/CAM');
-- تشغيل الفرايز المبرمجة CNC
&ins 'تشغيل الفرايز المبرمجة بالحاسب CNC','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشغيل الفرايز المبرمجة بالحاسب CNC' AND name='لغة انجليزية');
&ins 'تشغيل الفرايز المبرمجة بالحاسب CNC','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشغيل الفرايز المبرمجة بالحاسب CNC' AND name='ميكانيكا عامه');
&ins 'تشغيل الفرايز المبرمجة بالحاسب CNC','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشغيل الفرايز المبرمجة بالحاسب CNC' AND name='تكنولوجيا ومقايسات');
&ins 'تشغيل الفرايز المبرمجة بالحاسب CNC','التصميم والتصنيع بالحاسب CAD/CAM','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشغيل الفرايز المبرمجة بالحاسب CNC' AND name='التصميم والتصنيع بالحاسب CAD/CAM');
-- ماكينات الورش
&ins 'ماكينات الورش','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'ماكينات الورش' AND name='لغة انجليزية');
&ins 'ماكينات الورش','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'ماكينات الورش' AND name='ميكانيكا عامه');
&ins 'ماكينات الورش','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'ماكينات الورش' AND name='تكنولوجيا ومقايسات');
&ins 'ماكينات الورش','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'ماكينات الورش' AND name='رسم هندسى + CAD');
-- أعمال الصاج والأثاث المعدني
&ins 'أعمال الصاج والأثاث المعدني','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'أعمال الصاج والأثاث المعدني' AND name='لغة انجليزية');
&ins 'أعمال الصاج والأثاث المعدني','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'أعمال الصاج والأثاث المعدني' AND name='ميكانيكا عامه');
&ins 'أعمال الصاج والأثاث المعدني','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'أعمال الصاج والأثاث المعدني' AND name='تكنولوجيا ومقايسات');
&ins 'أعمال الصاج والأثاث المعدني','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'أعمال الصاج والأثاث المعدني' AND name='رسم فنى');
-- تمديدات شبكات المواسير
&ins 'تمديدات شبكات المواسير الصناعية والصحية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تمديدات شبكات المواسير الصناعية والصحية' AND name='لغة انجليزية');
&ins 'تمديدات شبكات المواسير الصناعية والصحية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تمديدات شبكات المواسير الصناعية والصحية' AND name='ميكانيكا عامه');
&ins 'تمديدات شبكات المواسير الصناعية والصحية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تمديدات شبكات المواسير الصناعية والصحية' AND name='تكنولوجيا ومقايسات');
&ins 'تمديدات شبكات المواسير الصناعية والصحية','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تمديدات شبكات المواسير الصناعية والصحية' AND name='رسم فنى');
-- سباكة المعادن
&ins 'سباكة المعادن','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'سباكة المعادن' AND name='لغة انجليزية');
&ins 'سباكة المعادن','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'سباكة المعادن' AND name='ميكانيكا عامه');
&ins 'سباكة المعادن','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'سباكة المعادن' AND name='تكنولوجيا ومقايسات');
&ins 'سباكة المعادن','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'سباكة المعادن' AND name='رسم هندسى + CAD');
-- الألوميتال
&ins 'الألوميتال','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الألوميتال' AND name='لغة انجليزية');
&ins 'الألوميتال','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الألوميتال' AND name='ميكانيكا عامه');
&ins 'الألوميتال','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الألوميتال' AND name='تكنولوجيا ومقايسات');
&ins 'الألوميتال','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الألوميتال' AND name='رسم فنى');
-- الصيانة الميكانيكية
&ins 'الصيانة الميكانيكية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الصيانة الميكانيكية' AND name='لغة انجليزية');
&ins 'الصيانة الميكانيكية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الصيانة الميكانيكية' AND name='ميكانيكا عامه');
&ins 'الصيانة الميكانيكية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الصيانة الميكانيكية' AND name='تكنولوجيا ومقايسات');
&ins 'الصيانة الميكانيكية','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الصيانة الميكانيكية' AND name='رسم هندسى + CAD');
-- صيانة ماكينات حياكة الملابس
&ins 'صيانة ماكينات حياكة الملابس','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة ماكينات حياكة الملابس' AND name='لغة انجليزية');
&ins 'صيانة ماكينات حياكة الملابس','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة ماكينات حياكة الملابس' AND name='ميكانيكا عامه');
&ins 'صيانة ماكينات حياكة الملابس','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة ماكينات حياكة الملابس' AND name='تكنولوجيا ومقايسات');
&ins 'صيانة ماكينات حياكة الملابس','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة ماكينات حياكة الملابس' AND name='رسم هندسى + CAD');
-- صيانة ماكينات الطباعة
&ins 'صيانة ماكينات الطباعة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة ماكينات الطباعة' AND name='لغة انجليزية');
&ins 'صيانة ماكينات الطباعة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة ماكينات الطباعة' AND name='ميكانيكا عامه');
&ins 'صيانة ماكينات الطباعة','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة ماكينات الطباعة' AND name='تكنولوجيا ومقايسات');
&ins 'صيانة ماكينات الطباعة','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة ماكينات الطباعة' AND name='رسم هندسى + CAD');
-- أشغال الحديد الزخرفي
&ins 'أشغال الحديد الزخرفي','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'أشغال الحديد الزخرفي' AND name='لغة انجليزية');
&ins 'أشغال الحديد الزخرفي','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'أشغال الحديد الزخرفي' AND name='ميكانيكا عامه');
&ins 'أشغال الحديد الزخرفي','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'أشغال الحديد الزخرفي' AND name='تكنولوجيا ومقايسات');
&ins 'أشغال الحديد الزخرفي','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'أشغال الحديد الزخرفي' AND name='رسم فنى');
-- صيانة التبريد وتكييف الهواء
&ins 'صيانة واصلاح اجهزة التبريد وتكييف الهواء','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة واصلاح اجهزة التبريد وتكييف الهواء' AND name='لغة انجليزية');
&ins 'صيانة واصلاح اجهزة التبريد وتكييف الهواء','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة واصلاح اجهزة التبريد وتكييف الهواء' AND name='ميكانيكا عامه');
&ins 'صيانة واصلاح اجهزة التبريد وتكييف الهواء','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة واصلاح اجهزة التبريد وتكييف الهواء' AND name='تكنولوجيا ومقايسات');
&ins 'صيانة واصلاح اجهزة التبريد وتكييف الهواء','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة واصلاح اجهزة التبريد وتكييف الهواء' AND name='رسم فنى');
-- فني بصريات
&ins 'فني بصريات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'فني بصريات' AND name='لغة انجليزية');
&ins 'فني بصريات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'فني بصريات' AND name='ميكانيكا عامه');
&ins 'فني بصريات','تكنولوجيا وحساب فنى','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'فني بصريات' AND name='تكنولوجيا وحساب فنى');
&ins 'فني بصريات','فيزياء','نظري',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'فني بصريات' AND name='فيزياء');
-- صيانة السيارات
&ins 'صيانة السيارات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة السيارات' AND name='لغة انجليزية');
&ins 'صيانة السيارات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة السيارات' AND name='ميكانيكا عامه');
&ins 'صيانة السيارات','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة السيارات' AND name='تكنولوجيا ومقايسات');
&ins 'صيانة السيارات','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة السيارات' AND name='رسم هندسى + CAD');
-- اصلاح ودهان هياكل السيارات
&ins 'اصلاح ودهان هياكل السيارات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'اصلاح ودهان هياكل السيارات' AND name='لغة انجليزية');
&ins 'اصلاح ودهان هياكل السيارات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'اصلاح ودهان هياكل السيارات' AND name='ميكانيكا عامه');
&ins 'اصلاح ودهان هياكل السيارات','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'اصلاح ودهان هياكل السيارات' AND name='تكنولوجيا ومقايسات');
&ins 'اصلاح ودهان هياكل السيارات','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'اصلاح ودهان هياكل السيارات' AND name='رسم فنى');
-- تشغيل ماكينات تصنيع البلاستيك
&ins 'تشغيل ماكينات تصنيع البلاستيك','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشغيل ماكينات تصنيع البلاستيك' AND name='لغة انجليزية');
&ins 'تشغيل ماكينات تصنيع البلاستيك','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشغيل ماكينات تصنيع البلاستيك' AND name='ميكانيكا عامه');
&ins 'تشغيل ماكينات تصنيع البلاستيك','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشغيل ماكينات تصنيع البلاستيك' AND name='تكنولوجيا ومقايسات');
&ins 'تشغيل ماكينات تصنيع البلاستيك','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشغيل ماكينات تصنيع البلاستيك' AND name='رسم هندسى + CAD');
-- فني البترول
&ins 'فني البترول والبتروكيماويات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'فني البترول والبتروكيماويات' AND name='لغة انجليزية');
&ins 'فني البترول والبتروكيماويات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'فني البترول والبتروكيماويات' AND name='ميكانيكا عامه');
&ins 'فني البترول والبتروكيماويات','تكنولوجيا بترول وحساب فني','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'فني البترول والبتروكيماويات' AND name='تكنولوجيا بترول وحساب فني');
&ins 'فني البترول والبتروكيماويات','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'فني البترول والبتروكيماويات' AND name='رسم هندسى + CAD');
-- طباعة الأوفست
&ins 'طباعة الأوفست','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'طباعة الأوفست' AND name='لغة انجليزية');
&ins 'طباعة الأوفست','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'طباعة الأوفست' AND name='ميكانيكا عامه');
&ins 'طباعة الأوفست','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'طباعة الأوفست' AND name='تكنولوجيا ومقايسات');
&ins 'طباعة الأوفست','رسم فنى طباعة','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'طباعة الأوفست' AND name='رسم فنى طباعة');
-- التجليد والتشطيب
&ins 'التجليد والتشطيب','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'التجليد والتشطيب' AND name='لغة انجليزية');
&ins 'التجليد والتشطيب','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'التجليد والتشطيب' AND name='ميكانيكا عامه');
&ins 'التجليد والتشطيب','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'التجليد والتشطيب' AND name='تكنولوجيا ومقايسات');
&ins 'التجليد والتشطيب','رسم فنى طباعة','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'التجليد والتشطيب' AND name='رسم فنى طباعة');
-- الكهرباء الصناعية
&ins 'الكهرباء الصناعية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الكهرباء الصناعية' AND name='لغة انجليزية');
&ins 'الكهرباء الصناعية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الكهرباء الصناعية' AND name='ميكانيكا عامه');
&ins 'الكهرباء الصناعية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الكهرباء الصناعية' AND name='تكنولوجيا ومقايسات');
&ins 'الكهرباء الصناعية','رسم الدوائر الكهربية','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الكهرباء الصناعية' AND name='رسم الدوائر الكهربية');
-- صيانة الأجهزة المنزلية
&ins 'صيانة واصلاح الأجهزة المنزلية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة واصلاح الأجهزة المنزلية' AND name='لغة انجليزية');
&ins 'صيانة واصلاح الأجهزة المنزلية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة واصلاح الأجهزة المنزلية' AND name='ميكانيكا عامه');
&ins 'صيانة واصلاح الأجهزة المنزلية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة واصلاح الأجهزة المنزلية' AND name='تكنولوجيا ومقايسات');
&ins 'صيانة واصلاح الأجهزة المنزلية','رسم فنى دوائر','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة واصلاح الأجهزة المنزلية' AND name='رسم فنى دوائر');
-- الإلكترونيات الصناعية
&ins 'الإلكترونيات الصناعية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الإلكترونيات الصناعية' AND name='لغة انجليزية');
&ins 'الإلكترونيات الصناعية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الإلكترونيات الصناعية' AND name='ميكانيكا عامه');
&ins 'الإلكترونيات الصناعية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الإلكترونيات الصناعية' AND name='تكنولوجيا ومقايسات');
&ins 'الإلكترونيات الصناعية','رسم دوائر إلكترونية','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الإلكترونيات الصناعية' AND name='رسم دوائر إلكترونية');
-- التحكم الآلي
&ins 'التحكم الآلي','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'التحكم الآلي' AND name='لغة انجليزية');
&ins 'التحكم الآلي','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'التحكم الآلي' AND name='ميكانيكا عامه');
&ins 'التحكم الآلي','تكنولوجيا','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'التحكم الآلي' AND name='تكنولوجيا');
&ins 'التحكم الآلي','رسم دوائر التحكم الآلى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'التحكم الآلي' AND name='رسم دوائر التحكم الآلى');
-- تشغيل وصيانة أنظمة الحاسب
&ins 'تشغيل وصيانة أنظمة وشبكات الحاسب','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشغيل وصيانة أنظمة وشبكات الحاسب' AND name='لغة انجليزية');
&ins 'تشغيل وصيانة أنظمة وشبكات الحاسب','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشغيل وصيانة أنظمة وشبكات الحاسب' AND name='ميكانيكا عامه');
&ins 'تشغيل وصيانة أنظمة وشبكات الحاسب','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشغيل وصيانة أنظمة وشبكات الحاسب' AND name='تكنولوجيا ومقايسات');
&ins 'تشغيل وصيانة أنظمة وشبكات الحاسب','صيانة اللاب توب والطابعات','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشغيل وصيانة أنظمة وشبكات الحاسب' AND name='صيانة اللاب توب والطابعات');
-- صيانة الهواتف المحمولة
&ins 'صيانة الهواتف المحمولة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة الهواتف المحمولة' AND name='لغة انجليزية');
&ins 'صيانة الهواتف المحمولة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة الهواتف المحمولة' AND name='ميكانيكا عامه');
&ins 'صيانة الهواتف المحمولة','تكنولوجيا','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة الهواتف المحمولة' AND name='تكنولوجيا');
&ins 'صيانة الهواتف المحمولة','رسم مخططات','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة الهواتف المحمولة' AND name='رسم مخططات');
-- الطاقة المتجددة
&ins 'الطاقة المتجددة الكهروضوئية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الطاقة المتجددة الكهروضوئية' AND name='لغة انجليزية');
&ins 'الطاقة المتجددة الكهروضوئية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الطاقة المتجددة الكهروضوئية' AND name='ميكانيكا عامه');
&ins 'الطاقة المتجددة الكهروضوئية','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الطاقة المتجددة الكهروضوئية' AND name='تكنولوجيا ومقايسات');
&ins 'الطاقة المتجددة الكهروضوئية','رسم ومحاكاة الدوائر','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الطاقة المتجددة الكهروضوئية' AND name='رسم ومحاكاة الدوائر');
-- برمجة الحاسب الآلي
&ins 'برمجة الحاسب الآلي','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'برمجة الحاسب الآلي' AND name='لغة انجليزية');
&ins 'برمجة الحاسب الآلي','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'برمجة الحاسب الآلي' AND name='ميكانيكا عامه');
&ins 'برمجة الحاسب الآلي','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'برمجة الحاسب الآلي' AND name='تكنولوجيا ومقايسات');
&ins 'برمجة الحاسب الآلي','ادارة المشروعات البرمجية','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'برمجة الحاسب الآلي' AND name='ادارة المشروعات البرمجية');
-- تصميم الجرافيك وبرمجة مواقع الانترنت
&ins 'تصميم الجرافيك وبرمجة مواقع الانترنت','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تصميم الجرافيك وبرمجة مواقع الانترنت' AND name='لغة انجليزية');
&ins 'تصميم الجرافيك وبرمجة مواقع الانترنت','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تصميم الجرافيك وبرمجة مواقع الانترنت' AND name='ميكانيكا عامه');
&ins 'تصميم الجرافيك وبرمجة مواقع الانترنت','التكنولوجيا والتقارير الفنية','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تصميم الجرافيك وبرمجة مواقع الانترنت' AND name='التكنولوجيا والتقارير الفنية');
&ins 'تصميم الجرافيك وبرمجة مواقع الانترنت','اسس البرمجة بلغة الجافا','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تصميم الجرافيك وبرمجة مواقع الانترنت' AND name='اسس البرمجة بلغة الجافا');
-- نجارة عامة
&ins 'نجارة عامة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'نجارة عامة' AND name='لغة انجليزية');
&ins 'نجارة عامة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'نجارة عامة' AND name='ميكانيكا عامه');
&ins 'نجارة عامة','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'نجارة عامة' AND name='تكنولوجيا ومقايسات');
&ins 'نجارة عامة','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'نجارة عامة' AND name='رسم فنى');
-- الملابس الجاهزة
&ins 'الملابس الجاهزة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الملابس الجاهزة' AND name='لغة انجليزية');
&ins 'الملابس الجاهزة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الملابس الجاهزة' AND name='ميكانيكا عامه');
&ins 'الملابس الجاهزة','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الملابس الجاهزة' AND name='تكنولوجيا ومقايسات');
&ins 'الملابس الجاهزة','رسم فنى نماذج','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الملابس الجاهزة' AND name='رسم فنى نماذج');
-- الصباغة وطباعة المنسوجات
&ins 'الصباغة وطباعة المنسوجات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'الصباغة وطباعة المنسوجات' AND name='لغة انجليزية');
&ins 'الصباغة وطباعة المنسوجات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'الصباغة وطباعة المنسوجات' AND name='ميكانيكا عامه');
&ins 'الصباغة وطباعة المنسوجات','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'الصباغة وطباعة المنسوجات' AND name='تكنولوجيا ومقايسات');
&ins 'الصباغة وطباعة المنسوجات','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'الصباغة وطباعة المنسوجات' AND name='رسم فنى');
-- تشكيل الزجاج
&ins 'تشكيل الزجاج','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشكيل الزجاج' AND name='لغة انجليزية');
&ins 'تشكيل الزجاج','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشكيل الزجاج' AND name='ميكانيكا عامه');
&ins 'تشكيل الزجاج','تكنولوجيا','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشكيل الزجاج' AND name='تكنولوجيا');
&ins 'تشكيل الزجاج','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشكيل الزجاج' AND name='رسم فنى');
-- تشكيل المعادن وتصنيع الحلي
&ins 'تشكيل المعادن وتصنيع الحلي','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تشكيل المعادن وتصنيع الحلي' AND name='لغة انجليزية');
&ins 'تشكيل المعادن وتصنيع الحلي','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تشكيل المعادن وتصنيع الحلي' AND name='ميكانيكا عامه');
&ins 'تشكيل المعادن وتصنيع الحلي','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تشكيل المعادن وتصنيع الحلي' AND name='تكنولوجيا ومقايسات');
&ins 'تشكيل المعادن وتصنيع الحلي','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تشكيل المعادن وتصنيع الحلي' AND name='رسم فنى');
-- تصنيع الاسطمبات
&ins 'تصنيع الاسطمبات','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'تصنيع الاسطمبات' AND name='لغة انجليزية');
&ins 'تصنيع الاسطمبات','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'تصنيع الاسطمبات' AND name='ميكانيكا عامه');
&ins 'تصنيع الاسطمبات','تكنولوجيا ومقايسات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'تصنيع الاسطمبات' AND name='تكنولوجيا ومقايسات');
&ins 'تصنيع الاسطمبات','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'تصنيع الاسطمبات' AND name='رسم هندسى + CAD');
-- سائق معدات ثقيلة
&ins 'سائق معدات ثقيلة','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'سائق معدات ثقيلة' AND name='لغة انجليزية');
&ins 'سائق معدات ثقيلة','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'سائق معدات ثقيلة' AND name='ميكانيكا عامه');
&ins 'سائق معدات ثقيلة','تكنولوجيا وحسابات','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'سائق معدات ثقيلة' AND name='تكنولوجيا وحسابات');
&ins 'سائق معدات ثقيلة','رسم هندسى + CAD','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'سائق معدات ثقيلة' AND name='رسم هندسى + CAD');
-- صيانة وإصلاح الأجهزة الطبية
&ins 'صيانة وإصلاح الأجهزة الطبية','لغة انجليزية','نظري',25,50,(SELECT MIN(id) FROM specializations),1 &chk 'صيانة وإصلاح الأجهزة الطبية' AND name='لغة انجليزية');
&ins 'صيانة وإصلاح الأجهزة الطبية','ميكانيكا عامه','نظري',25,50,(SELECT MIN(id) FROM specializations),2 &chk 'صيانة وإصلاح الأجهزة الطبية' AND name='ميكانيكا عامه');
&ins 'صيانة وإصلاح الأجهزة الطبية','تكنولوجيا وتقارير فنية','نظري',50,100,(SELECT MIN(id) FROM specializations),3 &chk 'صيانة وإصلاح الأجهزة الطبية' AND name='تكنولوجيا وتقارير فنية');
&ins 'صيانة وإصلاح الأجهزة الطبية','رسم فنى','عملي',50,100,(SELECT MIN(id) FROM specializations),4 &chk 'صيانة وإصلاح الأجهزة الطبية' AND name='رسم فنى');
COMMIT;
SELECT 'Subjects seeded: ' || COUNT(*) FROM subjects;
EXIT;
