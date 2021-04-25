import java.io.BufferedReader;
import java.io.IOException;
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

import javax.xml.catalog.Catalog;

public class SoundGoodMusic {

    private static Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:33061/sgm", "root", "hemen1993");
        connection.setAutoCommit(false);
        return connection;
    }

    public static void main(String[] args) throws IOException {

        try (Connection con = createConnection()) {

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            // BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Scanner in = new Scanner(System.in);
            System.out.println(
                    "Available options:\n 1- Instrument List\n 2- Rent An Instrument\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
            int choose = in.nextInt();

            while (!(choose > 4 || choose < 0)) {
                switch (choose) {
                case 1:
                    System.out.println("1- Instrument List\n ");
                    System.out.println();
                    System.out
                            .println("*************************List of available instruments*************************");
                    System.out.println();
                    listInstrument(con);
                    System.out.println(
                            "Available options:\n 2- Rent An Instrument\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
                    break;
                case 2:
                    System.out.println("2- Rent An Instrument\n");

                    System.out.println();
                    System.out.println("Your student ID:");
                    int student_id_rent = in.nextInt();
                    System.out.println("Type of instrument to rent:");
                    String instrument_type_rent = in.next();
                    rentInstrument(con, student_id_rent, instrument_type_rent);

                    // Ask user if it is okay to save
                    // boolean ok = askUserIfOkToSave();
                    // if (ok) {
                    // // store in database
                    // con.commit();
                    // System.out.println("\n>> Transaction COMMITTED.\n");
                    // } else {
                    // // discard
                    // con.rollback();
                    // System.out.println("\n>> Transaction ROLLED BACK.\n");
                    // }

                    System.out.println(
                            "Available options:\n 1- Instrument List\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
                    break;
                case 3:
                    System.out.println("3- Terminate an ongoing rental\n");
                    System.out.println();
                    System.out.println("Your student ID:");
                    int student_id_terminate = in.nextInt();
                    studentList(con, student_id_terminate);
                    System.out.println();
                    System.out.println("Select a rental_id of an instrument to terminate :");
                    int rental_id_terminate = in.nextInt();
                    terminateRental(con, student_id_terminate, rental_id_terminate);
                    // System.out.println("Rental termination complete!");
                    System.out.println(
                            "Available options:\n 1- Instrument List\n 2- Rent An Instrument\n 4- Back\n 0- Exit ");
                    break;
                case 4:
                    System.out.println(
                            "Available options:\n 1- Instrument List\n 2- Rent An Instrument\n 3- Terminate an ongoing rental\n 4- Back\n 0- Exit ");
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

        catch (SQLException | ClassNotFoundException | NumberFormatException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Rents an instrument by updating the database with student id and instrument
     * id. It checks first if the renting student has 2 or more current rentals and
     * if not succesfully rents.
     */
    public static void rentInstrument(Connection con, int student_id, String instrument_type) {
        String controlQuery = " SELECT count(student_id) as rentals FROM rental_instrument WHERE student_id='"
                + student_id + "' AND return_date IS NULL";

        String rentInstrumentStmt = "INSERT INTO rental_instrument(rental_id, student_id, instrument_type, monthly_price, start_date, instrument_brand)\n "
                + "SELECT (SELECT MAX(rental_id)+1 FROM rental_instrument),'" + student_id
                + "',(SELECT instrument_type FROM instrument WHERE  instrument_type='" + instrument_type
                + "'),(SELECT monthly_price FROM instrument WHERE instrument_type='" + instrument_type
                + "'), current_date(), (SELECT instrument_brand FROM instrument WHERE  instrument_type='"
                + instrument_type + "')";

        String updateQuery = "UPDATE instrument " + "SET quantity= quantity-1 WHERE instrument_type= '"
                + instrument_type + "' ";

        String showQuery = "SELECT instrument_type, instrument_brand, monthly_price FROM instrument WHERE instrument_type='"
                + instrument_type + "'";

        ResultSet rentals = null;
        ResultSet show = null;
        try {
            PreparedStatement stmt = con.prepareStatement(controlQuery);
            rentals = stmt.executeQuery(controlQuery);

            while (rentals.next()) {
                if (rentals.getString("rentals").equals("2")) {
                    System.out.println();
                    System.out.println("You already have rent two instuments");
                } else {
                    stmt = con.prepareStatement(rentInstrumentStmt);
                    stmt.executeUpdate(rentInstrumentStmt);

                    stmt = con.prepareStatement(updateQuery);
                    stmt.executeUpdate(updateQuery);

                    show = stmt.executeQuery(showQuery);
                    System.out.println();
                    while (show.next()) {
                        System.out.println("Selected:");
                        System.out.println("Instrument Type: " + show.getString("instrument_type") + "\n"
                                + "Instrument Brand: " + show.getString("instrument_brand") + "\n" + "Monthly Price: "
                                + show.getString("monthly_price") + "\n");
                    }

                }
            }

            con.commit();
            System.out.println("Rental complete!");

        } catch (SQLException e) {
            exceptionHandler(con);
            System.out.println("Rental Discard! ");
            System.out.println("Make sure to choose right instrument type!");
            System.out.println();
        } finally {
            closeResultSet(rentals);
            closeResultSet(show);
        }

    }

    /**
     * Terminates a rental of an instrument
     */
    public static void terminateRental(Connection con, int student_id, int rental_id) {
        ResultSet result = null;
        try {
            String cancelQuery = "UPDATE rental_instrument SET return_date = current_date() WHERE return_date IS NULL AND student_id = '"
                    + student_id + "' AND rental_id= '" + rental_id + "' ";

            PreparedStatement stmt = con.prepareStatement(cancelQuery);
            stmt.executeUpdate(cancelQuery);

            String updateQuery = "UPDATE instrument "
                    + "SET quantity = quantity+1 WHERE instrument_type IN (SELECT instrument_type FROM rental_instrument WHERE rental_id = '"
                    + rental_id + "')";

            stmt.executeUpdate(updateQuery);

            String showQuery = "SELECT instrument_type, instrument_brand, monthly_price, return_date FROM rental_instrument WHERE rental_id='"
                    + rental_id + "' AND student_id= '" + student_id + "' ";
            result = stmt.executeQuery(showQuery);
            System.out.println();
            if (result.next()) {
                System.out.println("Selected:");
                System.out.println("Instrument Type: " + result.getString("instrument_type") + "\n"
                        + "Instrument Brand: " + result.getString("instrument_brand") + "\n" + "Monthly Price: "
                        + result.getString("monthly_price") + "\n" + "Return Date: " + result.getString("return_date"));

                con.commit();
                System.out.println("Termination is successful!");
            } else {
                System.out.println("Please, choose the right Rental ID!");
                exceptionHandler(con);
                System.out.println("Termination discarded");

            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            closeResultSet(result);
        }

    }

    public static void studentList(Connection con, int student_id) {

        ResultSet result = null;
        try {
            String studentListQuery = "SELECT * From rental_instrument WHERE student_id = '" + student_id
                    + "' AND return_date IS NULL";

            PreparedStatement list = con.prepareStatement(studentListQuery);
            result = list.executeQuery(studentListQuery);

            while (result.next()) {
                System.out.println("Rental ID: " + result.getString("rental_id") + "   " + "Instrument Type: "
                        + result.getString("instrument_type") + "   " + "Rent Date: " + result.getString("start_date")
                        + "   " + "Monthly Price: " + result.getString("monthly_price") + "   " + "Instrument Brand: "
                        + result.getString("instrument_brand"));

            }
            con.commit();

        } catch (SQLException e) {
            exceptionHandler(con);
        } finally {
            closeResultSet(result);
        }

    }

    /**
     * Lists all the instruments
     */
    // public static void listInstrument(Statement st) throws SQLException,
    // SQLSyntaxErrorException {
    // String Query = "Select * from instrument where quantity > 0";
    // ResultSet result = st.executeQuery(Query);

    // while (result.next()) {
    // System.out.println("instrument_id: " + result.getString("instrument_id") + "
    // " + "instrument_type: "
    // + result.getString("instrument_type") + " " + "quantity: " +
    // result.getString("quantity")
    // + " " + "monthly_price: " + result.getString("monthly_price") + " " +
    // "instrument_brand: "
    // + result.getString("instrument_brand"));
    // System.out.println();
    // }
    // }

    public static void listInstrument(Connection con) {
        String Query = "Select * from instrument where quantity > 0";
        ResultSet result = null;
        try {
            PreparedStatement stmt = con.prepareStatement(Query);
            result = stmt.executeQuery(Query);

            while (result.next()) {
                System.out.println("instrument_id: " + result.getString("instrument_id") + "	    "
                        + "instrument_type: " + result.getString("instrument_type") + "     " + "quantity: "
                        + result.getString("quantity") + "     " + "monthly_price: " + result.getString("monthly_price")
                        + "	    " + "instrument_brand: " + result.getString("instrument_brand"));
                System.out.println();
            }
            con.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            exceptionHandler(con);
        } finally {
            closeResultSet(result);
        }

    }

    private static void closeResultSet(ResultSet result) {
        if (result == null) {
            return;
        }

        try {
            result.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void exceptionHandler(Connection connection) {
        try {
            connection.rollback();

        } catch (SQLException s) {
            System.out.println("exceptionHandler error. connection now valid?");
        }
    }

    private static boolean askUserIfOkToSave() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Is it okay to save?  yes/no: ");
        String input = scanner.nextLine();

        scanner.close();

        return input.equalsIgnoreCase("yes");
    }
}
