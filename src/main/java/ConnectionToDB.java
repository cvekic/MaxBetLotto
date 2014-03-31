import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mladjan on 30.3.2014.
 */
public class ConnectionToDB {
    private java.sql.Connection con = null;
    private PreparedStatement pst = null;
    private ResultSet rs = null;
    private String url = "jdbc:mysql://localhost/maxbetlotto?";
    private String user = "root";
    private String password = "";

    public List<Integer> getAllTickets() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        List<Integer> ticketsId = new ArrayList<Integer>();
        final String DRIVER = "com.mysql.jdbc.Driver";
        Connection subject;
        Class.forName(DRIVER).newInstance();
        subject = DriverManager.getConnection(url, user, password);
        Statement stmt = subject.createStatement();
        rs = stmt.executeQuery("SELECT id FROM tickets");
        while(rs.next()) {
            Integer ticket = 0;
            ticket = (rs.getInt("id"));
            ticketsId.add(ticket);
        }
        return ticketsId;
    }
    public void insertIntoDB(Integer id, String combination) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        con = DriverManager.getConnection(url, user, password);
        String query = " insert into tickets values (?, ?)";
        PreparedStatement preparedStmt = con.prepareStatement(query);
        preparedStmt.setInt(1, id);
        preparedStmt.setString (2, combination);
        preparedStmt.execute();
        con.close();
    }

    public void deleteFromDB(Integer id) throws SQLException {
        con = DriverManager.getConnection(url, user, password);
        String query = " delete from tickets where id=?";
        PreparedStatement preparedStmt = con.prepareStatement(query);
        preparedStmt.setInt(1, id);
        preparedStmt.execute();
        con.close();
    }


}
