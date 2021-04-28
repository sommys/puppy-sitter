package hu.bme.aut.android.puppysitter.model

class Dog(): User() {
    protected val breed: String = ""
    protected var weight: Int = 0
    protected var activity: Int = 0
}