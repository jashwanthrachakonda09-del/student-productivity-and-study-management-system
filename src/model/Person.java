package model;

// UNIT II - Abstract class.
// You cannot create an object of Person directly. Child classes (Student) complete it.
public abstract class Person {

    protected String name;      // protected: child classes can use it directly

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Abstract method: no body here, every child class MUST write its own version.
    public abstract String getRole();
}
