import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mladjan on 30.3.2014.
 */
public class ConnectionToDB {
    private java.sql.Connection con = null;
    private PreparedStatement pStmt = null;
    Statement stmt = null;
    public ResultSet rs = null;
    private String url = "jdbc:mysql://localhost/maxbetlotto?";
    private String user = "root";
    private String password = "";
    final String DRIVER = "com.mysql.jdbc.Driver";

    public List<Integer> getAllTickets() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        List<Integer> ticketsId = new ArrayList<Integer>();
        Class.forName(DRIVER).newInstance();
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT id FROM tickets");
        while(rs.next()) {
            Integer ticket = 0;
            ticket = (rs.getInt("id"));
            ticketsId.add(ticket);
        }
        con.close();
        return ticketsId;
    }
    public void insertIntoDB(Integer id, String combination) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        con = DriverManager.getConnection(url, user, password);
        String query = " INSERT INTO tickets VALUES (?, ?)";
        pStmt = con.prepareStatement(query);
        pStmt.setInt(1, id);
        pStmt.setString(2, combination);
        pStmt.execute();
        con.close();
    }
    public void deleteFromDB(Integer id) throws SQLException {
        con = DriverManager.getConnection(url, user, password);
        String query = " DELETE FROM tickets WHERE id=?";
        pStmt = con.prepareStatement(query);
        pStmt.setInt(1, id);
        pStmt.execute();
        con.close();
    }
}
