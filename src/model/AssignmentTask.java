package model;

// UNIT II - Inheritance: an assignment / record / report that must be submitted.
public class AssignmentTask extends Task {

    public AssignmentTask(String subject, String title, String deadline, String priority) {
        super(subject, title, deadline, priority);
    }

    public AssignmentTask(int id, String subject, String title, String deadline, String priority, boolean completed) {
        super(id, subject, title, deadline, priority, completed);
    }

    @Override
    public String getTaskType() {
        return "Assignment";
    }

    // Method overriding + super keyword: reuse the parent's message and add something extra
    @Override
    public String getReminderMessage() {
        return "ASSIGNMENT - " + super.getReminderMessage();
    }
}
