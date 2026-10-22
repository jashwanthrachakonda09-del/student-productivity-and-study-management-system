package model;

import util.Reminder;

// UNIT II - Abstract class that also implements the Reminder interface.
// StudyTask and AssignmentTask extend this class.
public abstract class Task implements Reminder {

    // UNIT I - private fields (access control). Other classes use getters/setters.
    private int id;
    private String subject;
    private String title;
    private String deadline;      // stored as text: yyyy-MM-dd
    private String priority;      // Low, Medium or High
    private boolean completed;
    private String notes = "";    // optional notes written by the student

    // Constructor 1: for a NEW task (id is given later by the database)
    public Task(String subject, String title, String deadline, String priority) {
        this.subject = subject;
        this.title = title;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = false;
    }

    // Constructor 2 (overloading): for a task loaded from the database
    public Task(int id, String subject, String title, String deadline, String priority, boolean completed) {
        this(subject, title, deadline, priority);   // reuse constructor 1
        this.id = id;
        this.completed = completed;
    }

    // Abstract method: each child class says its own type
    public abstract String getTaskType();

    // UNIT II - final method: child classes are NOT allowed to override it
    // Status is Completed, Overdue (deadline already passed) or Pending.
    public final String getStatusText() {
        if (completed) {
            return "Completed";
        }
        if (isOverdue()) {
            return "Overdue";
        }
        return "Pending";
    }

    // A task is overdue if it is not completed and its deadline is before today.
    // java.util.Date has a before() method to compare two dates.
    public boolean isOverdue() {
        if (completed) {
            return false;
        }
        java.util.Date deadlineDate = java.sql.Date.valueOf(deadline);
        java.util.Date todayDate = java.sql.Date.valueOf(util.Helper.today());
        return deadlineDate.before(todayDate);
    }

    // ----- Methods of the Reminder interface -----
    @Override
    public boolean isDueSoon() {
        if (completed) {
            return false;
        }
        long oneDay = 24L * 60 * 60 * 1000;     // one day in milliseconds
        long deadlineTime = java.sql.Date.valueOf(deadline).getTime();
        long now = new java.util.Date().getTime();
        long difference = deadlineTime - now;
        // due today  -> difference between -1 day and 0
        // due tomorrow -> difference between 0 and +1 day
        return difference >= -oneDay && difference <= oneDay;
    }

    @Override
    public String getReminderMessage() {
        return subject + ": " + title + " is due on " + deadline;
    }

    // ----- Getters and setters -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // UNIT II - Object class methods we override
    @Override
    public String toString() {
        return "[" + getTaskType() + "] " + subject + " - " + title
                + " (deadline " + deadline + ", " + priority + ") - " + getStatusText();
    }

    // Two tasks are equal if they have the same id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;     // casting Object to Task
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
