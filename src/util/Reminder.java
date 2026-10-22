package util;

// UNIT II - Interface.
// An interface only declares methods. The class that implements it (Task) must write the body.
public interface Reminder {

    boolean isDueSoon();            // true if the deadline is today or tomorrow

    String getReminderMessage();    // text shown in the reminder pop-up
}
