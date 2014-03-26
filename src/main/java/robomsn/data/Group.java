package robomsn.data;

/**
 * Contact group
 */
public class Group
{
    private String name;
    private int number;

    public Group(String name, int number)
    {
        this.name = name;
        this.number = number;
    }
    
    public String getName()
    {
        return name;
    }
    public int getNumber()
    {
        return number;
    }
}
