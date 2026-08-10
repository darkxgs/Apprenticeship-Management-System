# Phase 2: Composite Subjects (30/70) implementation

The goal is to support subjects that are split into two parts (e.g., 30 marks for research/homework and 70 marks for the exam) under a single main subject. This should be managed in the Subjects settings and displayed as a single grouped card in the Data Entry page.

## User Review Required

> [!IMPORTANT]
> **Grouped Rendering**: In the Data Entry page, composite subjects will appear in a single card with multiple inputs. We need to ensure that the calculation of the "Total" for the student correctly aggregates these sub-subjects.

> [!TIP]
> **Sub-names**: When creating a composite subject, you should provide names for the sub-parts (e.g., "نظري", "تحريري") to distinguish them in the UI.

## Proposed Changes

### [Component Name] Subjects Management

#### [MODIFY] [SubjectService.java](file:///c:/Users/seifd/OneDrive/Desktop/Java%20Project/src/main/java/com/pvtd/students/services/SubjectService.java)
- Ensure all CRUD operations correctly handle `parent_subject_id` and `sub_name`.
- Add a utility to fetch children for a given parent subject.

#### [MODIFY] [SubjectsPage.java](file:///c:/Users/seifd/OneDrive/Desktop/Java%20Project/src/main/java/com/pvtd/students/ui/pages/SubjectsPage.java)
- Update the subject list to visually indent or group sub-subjects under their parents.
- Add a "Split" action to existing dynamic subjects.

### [Component Name] Data Entry UI

#### [MODIFY] [DataEntryPage.java](file:///c:/Users/seifd/OneDrive/Desktop/Java%20Project/src/main/java/com/pvtd/students/ui/pages/DataEntryPage.java)
- Modify `renderSubjectGroup` to identify composite subjects and group them.
- Update `populateGradesIntoUI` and `extractGradesFromUI` to support the new field mapping.
- Update the calculation logic (`recalculate()`) to handle sub-marks.

## Open Questions

1. **Total Mark Display**: For composite subjects, should we display the "Total" of the two sub-subjects in the card as well, or just let them be separate inputs?
2. **Tab Indexing**: Should the "Enter" key move from the first part of a composite subject to the second part, or skip to the next subject entirely? (I suggest moving to the second part first).

## Verification Plan

### Automated Tests
- N/A (Manual UI testing required)

### Manual Verification
1. Create a "Technology" subject with max 100.
2. Create a sub-subject "Review" with max 30, linked to "Technology".
3. Create another sub-subject "Exam" with max 70, linked to "Technology".
4. Go to Data Entry, select a student in that profession.
5. Verify that "Technology" appears as one card with two fields.
6. Enter 25 and 60, verify it saves and recalculates correctly.
