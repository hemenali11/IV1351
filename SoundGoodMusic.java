import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Scanner;








public class SoundGoodMusic {
    
   

    

    
    

    public static void main(String[] args)  {
        
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con=DriverManager.getConnection("jdbc:mysql://127.0.0.1:33061/sgm","root","hemen1993");
            Statement SMT=con.createStatement();
            //ResultSet rs = stmt.executeQuery("select first_name from person where age<30;");
            /*while(rs.next())
            {
                System.out.println(rs.getString(1));
            }*/

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        Scanner in = new Scanner(System.in);
	    System.out.println("Available options:\n 1- Instrument List\n 2- Rent An Instrument\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
	    int choose = in.nextInt();

        
        while(!(choose > 4 || choose < 0)) {
            switch(choose){
        case 1:
            System.out.println("*************************List of available instruments*************************");
            System.out.println();
            listInstrument(SMT);
            break;
        case 2:
            System.out.println("Your student ID:");
            int student_id_rent = in.nextInt();
            System.out.println("Type of instrument to rent:");
            String instrument_type_rent = in.next();
            rentInstrument(SMT, student_id_rent,instrument_type_rent);
            break;
        case 3:
            System.out.println("Your student ID:");
            int student_id_terminate = in.nextInt();
            System.out.println("Type of instrument to terminate:");
            String instrument_type_terminate = in.next();
            terminateRental(SMT, student_id_terminate, instrument_type_terminate);
            break;
        case 4:
            System.out.println("Available options:\n 1- Instrument List\n 2- Rent An Instrument\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
            break;
        case 0:
            System.out.println("Thank You!");
            return;
        default:
            System.out.println("No such choice!");
        }
        System.out.println();
		choose = in.nextInt();
        }
        

        con.close();
        }

        catch(Exception e)
        {
            System.out.println(e);
        }
    }


     /**
     * Rents an instrument by updating the database with student id and instrument id.
     * It checks first if the renting student has 2 or more current rentals and if not succesfully rents.
     */
    public static void rentInstrument(Statement instrument, int student_id, String instrument_type) throws SQLException, SQLSyntaxErrorException {
        String  controlQuery =
            " SELECT count(student_id) as rentals FROM rental_instrument WHERE student_id='" + student_id + "'";
        
        ResultSet rentals = instrument.executeQuery(controlQuery);

        if(rentals.next())
        {
            if(rentals.getString("rentals").equals("2")){
                System.out.println();
                System.out.println("You already have rent two instuments");
            }
            else{
                String rentInstrumentStmt= 
                "INSERT INTO rental_instrument(rental_id, student_id, instrument_type, monthly_price, start_date, instrument_brand)\n " + 
                "SELECT (SELECT MAX(rental_id)+1 FROM rental_instrument),'" + student_id +
                "',(SELECT instrument_type FROM instrument WHERE  instrument_type='" + instrument_type + 
                "'),(SELECT monthly_price FROM instrument WHERE instrument_type='" + instrument_type + 
                "'), current_date(), (SELECT instrument_brand FROM instrument WHERE  instrument_type='" 
                + instrument_type + "')";
                instrument.executeUpdate(rentInstrumentStmt);

                String updateQuery = "UPDATE instrument " +  
                                    "SET quantity= quantity-1 WHERE instrument_type= '" + instrument_type + "' ";

                instrument.executeUpdate(updateQuery);
            }
        }
    }


     /**
     * Terminates a rental of an instrument
     */
    public static void terminateRental (Statement instrument, int student_id, String instrument_type)throws SQLException, SQLSyntaxErrorException
    {
        String cancelQuery = 
        "UPDATE rental_instrument SET return_date = current_date() WHERE return_date IS NULL AND student_id = '" 
        + student_id + "' AND instrument_type= '" + instrument_type + "' ";

        instrument.executeUpdate(cancelQuery);

        String updateQuery = "UPDATE instrument " +  
                                    "SET quantity = quantity+1 WHERE instrument_type= '" + instrument_type + "'";

        instrument.executeUpdate(updateQuery);
    }

    


    /**
     * Lists all the instruments
     */
    public static void listInstrument (Statement st) throws SQLException, SQLSyntaxErrorException {
        String Query = "Select * from instrument where quantity > 0";
        ResultSet result = st.executeQuery(Query);

        while(result.next()) {
			System.out.println("instrument_id: " + result.getString("instrument_id")
			+ "	    " + "instrument_type: " + result.getString("instrument_type")
			+ "     " + "quantity: " +result.getString("quantity")
			+ "     " + "monthly_price: " +result.getString("monthly_price")
            + "	    " + "instrument_brand: " +result.getString("instrument_brand") );
		System.out.println();
		}
    }
}
