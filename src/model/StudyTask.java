package model;

// UNIT II - Inheritance: a normal study task (reading, practice, revision...)
public class StudyTask extends Task {

    public StudyTask(String subject, String title, String deadline, String priority) {
        super(subject, title, deadline, priority);
    }

    public StudyTask(int id, String subject, String title, String deadline, String priority, boolean completed) {
        super(id, subject, title, deadline, priority, completed);
    }

    @Override
    public String getTaskType() {
        return "Study";
    }
}
