import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

//Justin Meisner and Eliot Sollinger
//EECS 484 P3

//json.simple 1.1
// import org.json.simple.JSONObject;
// import org.json.simple.JSONArray;

// Alternate implementation of JSON modules.
import org.json.JSONObject;
import org.json.JSONArray;

public class GetData{
    
    static String prefix = "project2.";
    
    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;
    
    // You must refer to the following variables for the corresponding 
    // tables in your database

    String cityTableName = null;
    String userTableName = null;
    String friendsTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;
    String programTableName = null;
    String educationTableName = null;
    String eventTableName = null;
    String participantTableName = null;
    String albumTableName = null;
    String photoTableName = null;
    String coverPhotoTableName = null;
    String tagTableName = null;

    // This is the data structure to store all users' information
    // DO NOT change the name
    JSONArray users_info = new JSONArray();     // declare a new JSONArray

    
    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
    super();
    String dataType = u;
    oracleConnection = c;
    // You will use the following tables in your Java code
    cityTableName = prefix+dataType+"_CITIES";
    userTableName = prefix+dataType+"_USERS";
    friendsTableName = prefix+dataType+"_FRIENDS";
    currentCityTableName = prefix+dataType+"_USER_CURRENT_CITIES";
    hometownCityTableName = prefix+dataType+"_USER_HOMETOWN_CITIES";
    programTableName = prefix+dataType+"_PROGRAMS";
    educationTableName = prefix+dataType+"_EDUCATION";
    eventTableName = prefix+dataType+"_USER_EVENTS";
    albumTableName = prefix+dataType+"_ALBUMS";
    photoTableName = prefix+dataType+"_PHOTOS";
    tagTableName = prefix+dataType+"_TAGS";
    }
    
    
    
    
    //implement this function

    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException{ 

        JSONArray users_info = new JSONArray();
        
    // Your implementation goes here....

        Statement stmt = oracleConnection.createStatement();
        
        ResultSet rst1 = stmt.executeQuery(
            "SELECT u.User_ID, u.First_Name, u.Last_Name, u.gender, u.DAY_OF_BIRTH, u.MONTH_OF_BIRTH, u.YEAR_OF_BIRTH, c1.city_name, " 
            + "c1.country_name, c1.state_name, c2.city_name, c2.country_name, c2.state_name" +
            " FROM " + userTableName + " u "
            + "LEFT JOIN " + hometownCityTableName + " h ON h.User_ID  = u.User_ID "
            + "LEFT JOIN " + cityTableName + " c1 ON c1.city_ID = h.hometown_city_ID "
            + "LEFT JOIN " + currentCityTableName + " c on c.User_ID = u.User_ID "
            + "LEFT JOIN " + cityTableName + " c2 ON c2.city_ID = c.current_city_ID"
            );
        
        while(rst1.next()){
            JSONObject ins = new JSONObject();

            ins.put("user_id", rst1.getInt(1));
            Integer temp = rst1.getInt(1);
            String current_user = temp.toString();

            ins.put("first_name", rst1.getString(2));
            ins.put("last_name", rst1.getString(3));
            ins.put("gender" , rst1.getString(4));
            ins.put("DOB", rst1.getInt(5));
            ins.put("MOB", rst1.getInt(6));
            ins.put("YOB" , rst1.getInt(7));
            
            JSONObject ht = new JSONObject();
            ht.put("city", rst1.getString(8));
            ht.put("country", rst1.getString(9));
            ht.put("state", rst1.getString(10));
            ins.put("hometown", ht);
            
            JSONObject ct = new JSONObject();
            ct.put("city", rst1.getString(11));
            ct.put("country", rst1.getString(12));
            ct.put("state", rst1.getString(13));
            ins.put("current", ct);


            Statement stmt2 = oracleConnection.createStatement();
            ResultSet rst2 = stmt2.executeQuery(
            "SELECT user2_ID " +
            "FROM " + friendsTableName + " " +
            "WHERE user1_ID < user2_ID AND user1_ID = " + current_user
            );
            
            JSONArray ff = new JSONArray();
            while(rst2.next()){
                ff.put(rst2.getInt(1));
                }
            ins.put("friends", ff);
            rst2.close();
            stmt2.close();
                
            users_info.put(ins);
        }
        
        
        rst1.close();
        stmt.close();
        return users_info;

    }

    // This outputs to a file "output.json"
    public void writeJSON(JSONArray users_info) {
    // DO NOT MODIFY this function
    try {
        FileWriter file = new FileWriter(System.getProperty("user.dir")+"/output.json");
        file.write(users_info.toString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
        
    }
}
