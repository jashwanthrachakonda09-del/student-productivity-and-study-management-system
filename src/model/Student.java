package model;

// UNIT II - Inheritance: Student "is a" Person (extends).
// UNIT I  - Constructors, constructor overloading, this keyword, access control (private + getters).
public class Student extends Person {

    private int id;
    private String username;

    // Constructor 1: used when registering (no id yet)
    public Student(String name, String username) {
        super(name);               // super calls the Person constructor
        this.username = username;  // this.username = the field, username = the parameter
    }

    // Constructor 2 (overloaded): used when we read a student from the database
    public Student(int id, String name, String username) {
        this(name, username);      // this(...) calls Constructor 1
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // Method overriding: we write the body of the abstract method from Person
    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}
