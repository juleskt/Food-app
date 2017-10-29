package ec601.aty.food_app;


public class User {

    public String name;
    public String accounttype;

    public User(){

    }

    public User(String accounttype, String name){
        this.name = name;
        this.accounttype = accounttype;
    }
}
