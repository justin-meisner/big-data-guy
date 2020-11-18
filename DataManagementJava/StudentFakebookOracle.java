/*
EECS 484 - Project 2
StudentFakebookOracle.java
Justin Meisner & Eliot Sollinger
*/


package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }
    
    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                "SELECT COUNT(*) AS Birthed, Month_of_Birth " +         // select birth months and number of uses with that birth month
                "FROM " + UsersTable + " " +                            // from all users
                "WHERE Month_of_Birth IS NOT NULL " +                   // for which a birth month is available
                "GROUP BY Month_of_Birth " +                            // group into buckets by birth month
                "ORDER BY Birthed DESC, Month_of_Birth ASC");           // sort by users born in that month, descending; break ties by birth month
            
            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) {                       // step through result rows/records one by one
                if (rst.isFirst()) {                   // if first record
                    mostMonth = rst.getInt(2);         //   it is the month with the most
                }
                if (rst.isLast()) {                    // if last record
                    leastMonth = rst.getInt(2);        //   it is the month with the least
                }
                total += rst.getInt(1);                // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);
            
            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " +                // select ID, first name, and last name
                "FROM " + UsersTable + " " +                              // from all users
                "WHERE Month_of_Birth = " + mostMonth + " " +             // born in the most popular birth month
                "ORDER BY User_ID");                                      // sort smaller IDs first
                
            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " +                // select ID, first name, and last name
                "FROM " + UsersTable + " " +                              // from all users
                "WHERE Month_of_Birth = " + leastMonth + " " +            // born in the least popular birth month
                "ORDER BY User_ID");                                      // sort smaller IDs first
                
            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close();                            // if you close the statement first, the result set gets closed automatically

            return info;

        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }
    
    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            FirstNameInfo info = new FirstNameInfo();
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name FROM " + UsersTable + 
                " WHERE LENGTH(First_Name) = (SELECT MAX(LENGTH(First_Name)) FROM " + UsersTable + 
                " ) ORDER BY First_Name");
            //resturns rows that have first names
            while(rst.next()){
                info.addLongName(rst.getString(1));    
            }
            // rst.close();
            
            rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name FROM " + UsersTable + 
                " WHERE LENGTH(First_Name) = (SELECT MIN(LENGTH(First_Name)) FROM " + UsersTable + 
                " ) ORDER BY First_Name");

            while(rst.next()){
                info.addShortName(rst.getString(1));
            }
            // rst.close();

            rst = stmt.executeQuery(
                "SELECT COUNT (*) AS numName, First_Name FROM " + UsersTable +
                " GROUP BY First_Name ORDER BY numName DESC, First_Name ASC"
                );

            Integer count = -1;
            while(rst.next()){
                if(rst.isFirst() || rst.getInt(1) == count){
                    info.addCommonName(rst.getString(2));
                    info.setCommonNameCount(rst.getInt(1));
                    count = rst.getInt(1);
                }
            }
            rst.close();
            stmt.close();
            return info;
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }
    
    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */

            ResultSet rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name FROM " + UsersTable +
                " WHERE User_ID NOT IN (SELECT DISTINCT User1_ID FROM " + FriendsTable +
                " UNION SELECT DISTINCT User2_ID FROM " + FriendsTable + ") ORDER BY User_ID"
                );

            Integer count_no_friends = 0;
            while(rst.next()){
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
                count_no_friends += 1;
            }
        rst.close();
        stmt.close(); 
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }
    
    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */

            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT u.User_ID, u.First_Name, u.Last_Name" + 
                " FROM " + UsersTable + " u, " + HometownCitiesTable + " h, " + CurrentCitiesTable + " c"
                + " WHERE u.User_ID = h.User_ID AND u.User_ID = c.User_ID AND Current_City_ID <> Hometown_City_ID " +
                " ORDER BY u.User_ID");
            //Integer count_lives_away = 0;
            while(rst.next()){
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
                //count_lives_away+= 1;
            }
        rst.close();
        stmt.close(); 
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }
    
    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */

            stmt.executeUpdate( 
                " CREATE VIEW tag_counts AS " +
                " SELECT t.Tag_Photo_ID, COUNT(t.Tag_Photo_ID) AS num_Tags, p.Photo_link, a.Album_name" +
                " FROM " + TagsTable + " t " + 
                " LEFT JOIN " + PhotosTable + " p ON t.Tag_Photo_ID = p.Photo_ID " + 
                " LEFT JOIN " + AlbumsTable + " a ON a.Album_ID = p.Album_ID " + 
                " GROUP BY t.Tag_Photo_ID, a.Album_ID, p.Photo_link, a.Album_name" +
                " ORDER BY num_Tags DESC, t.Tag_Photo_ID ASC");


            ResultSet rst2 = stmt.executeQuery(
                "SELECT p1.Photo_ID, t.num_Tags, p1.Photo_link, a.Album_name, a.Album_ID, u.User_ID, u.First_Name, u.Last_Name" + 
                " FROM " + PhotosTable + " p1, " + UsersTable + " u, " + AlbumsTable + " a, tag_counts t, " + TagsTable + " t3 " + 
                " WHERE  p1.Photo_ID = t.Tag_Photo_ID AND t.Tag_Photo_ID = t3.Tag_Photo_ID AND t3.Tag_Subject_ID = u.User_ID AND p1.Album_ID = a.Album_ID" +
                " ORDER BY t.num_Tags DESC, p1.Photo_ID ASC, u.User_ID ASC");

            Integer count = 0;
            while(rst2.next() && count < num){
                long p_id = 0;
                String link = " ";
                String album_name = "";
                long num_tags = 0;
                long album_id = 0;

                p_id = rst2.getLong(1);
                num_tags = rst2.getLong(2);
                link = rst2.getString(3);
                album_name = rst2.getString(4);
                album_id = rst2.getLong(5);

                PhotoInfo p = new PhotoInfo(p_id, album_id, link, album_name);
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);

                long tag_id = 0;
                String tag_fn = "";
                String tag_ln = "";

                tag_id = rst2.getLong(6);
                tag_fn = rst2.getString(7);
                tag_ln = rst2.getString(8);
                UserInfo u = new UserInfo(tag_id, tag_fn, tag_ln);
                tp.addTaggedUser(u);

                for (long i = 1; i < num_tags; ++i){
                    rst2.next();
                    tag_id = rst2.getLong(6);
                    tag_fn = rst2.getString(7);
                    tag_ln = rst2.getString(8);
                    tp.addTaggedUser(new UserInfo(tag_id, tag_fn, tag_ln));
                }
                results.add(tp);
                count++;
            }

            stmt.executeUpdate("DROP VIEW tag_counts");
            rst2.close();
            stmt.close(); 
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);

                        */

            // stmt.executeUpdate("DROP VIEW tagged_photo_info");
            // stmt.executeUpdate("DROP VIEW tagged_together");
            // stmt.executeUpdate("DROP VIEW not_friends");

            stmt.executeUpdate( 
                " CREATE VIEW not_friends AS " + 
                " SELECT DISTINCT u1.User_ID AS u1_id, u2.User_ID AS u2_id" +
                " FROM " + UsersTable + " u1, " + UsersTable + " u2 " +
                " WHERE u1.User_ID < u2.User_ID " +
                " AND (Abs(u1.Year_OF_BIRTH - u2.Year_OF_BIRTH) <= " + yearDiff + " AND Abs(u2.Year_OF_BIRTH - u1.Year_OF_BIRTH) <= " + yearDiff + ") " +  
                " AND (u1.gender = u2.gender) " +
                " MINUS " + 
                " (SELECT f.User1_ID, f.User2_ID FROM " + FriendsTable + " f)"
                );

            stmt.executeUpdate( 
                " CREATE VIEW tagged_together AS " +
                " SELECT DISTINCT u11.User_ID AS t_User1_ID, u12.User_ID AS t_User2_ID, COUNT(*) AS times_together" +
                " FROM " + UsersTable + " u11, " + UsersTable + " u12, " + TagsTable + " t1, " + TagsTable + " t12, not_friends n1 " +
                " WHERE (u11.User_ID = n1.u1_id AND u12.User_ID = n1.u2_id) AND (u11.User_ID = t1.Tag_subject_ID AND u12.User_ID = t12.Tag_subject_ID AND t1.Tag_Photo_ID = t12.Tag_Photo_ID) " + 
                " GROUP BY u11.User_ID, u12.User_ID " +
                " ORDER BY COUNT(*) DESC"
                );

            stmt.executeUpdate(
                " CREATE VIEW tagged_photo_info AS " + 
                " SELECT DISTINCT t.Tag_Photo_ID AS tagger_ID, ayo.t_User1_ID AS Final_u1_ID, ayo.t_User2_ID AS Final_u2_ID, ayo.times_together AS final_times_together " + 
                " FROM " + TagsTable + " t, tagged_together ayo, " + TagsTable + " t2 " + 
                " WHERE t.Tag_subject_ID = ayo.t_User1_ID AND t2.Tag_subject_ID = ayo.t_User2_ID AND t.Tag_Photo_ID = t2.Tag_Photo_ID"
                );

            ResultSet rst = stmt.executeQuery(
                "SELECT u5.User_ID, u5.Year_of_birth, u5.First_Name, u5.Last_Name, u6.User_ID, u6.Year_of_birth, u6.First_Name, u6.Last_Name, p.Photo_ID, a.Album_ID, p.Photo_link, a.Album_name" + 
                " FROM " + UsersTable + " u5, " + UsersTable + " u6, tagged_photo_info t, " + PhotosTable + " p, " + AlbumsTable + " a " +
                " WHERE (u5.User_ID = t.Final_u1_ID AND u6.User_ID = t.Final_u2_ID) AND (t.tagger_ID = p.Photo_ID AND p.Album_ID = a.Album_ID) " +  
                " ORDER BY t.final_times_together DESC, t.Final_u1_ID ASC, t.Final_u2_ID ASC, p.Photo_ID ASC "
                );

            long counter = 1;
            long u1_id = 0;
            long u1_yr = 0;
            String u1_fn = " ";
            String u1_ln = " ";
            long u2_id = 0;
            long u2_yr = 0;
            String u2_fn = " ";
            String u2_ln = " ";
            long p_id = 0;
            long a_id = 0;
            String p_link = " ";
            String a_name = " ";

            rst.next();

            u1_id = rst.getLong(1);
            u1_yr = rst.getLong(2);
            u1_fn = rst.getString(3);
            u1_ln = rst.getString(4);
            u2_id = rst.getLong(5);
            u2_yr = rst.getLong(6);
            u2_fn = rst.getString(7);
            u2_ln = rst.getString(8);

            UserInfo u1 = new UserInfo(u1_id, u1_fn, u1_ln);
            UserInfo u2 = new UserInfo(u2_id, u2_fn, u2_ln);
            MatchPair mp = new MatchPair(u1, u1_yr, u2, u2_yr);

            p_id = rst.getLong(9);
            a_id = rst.getLong(10);
            p_link = rst.getString(11);
            a_name = rst.getString(12);

            PhotoInfo p = new PhotoInfo(p_id, a_id, p_link, a_name);
            mp.addSharedPhoto(p);
            results.add(mp);

            while (counter < num && rst.next()){
                if (rst.getLong(1) != u1_id || rst.getLong(5) != u2_id){
                    u1_id = rst.getLong(1);
                    u1_yr = rst.getLong(2);
                    u1_fn = rst.getString(3);
                    u1_ln = rst.getString(4);
                    u2_id = rst.getLong(5);
                    u2_yr = rst.getLong(6);
                    u2_fn = rst.getString(7);
                    u2_ln = rst.getString(8);

                    u1 = new UserInfo(u1_id, u1_fn, u1_ln);
                    u2 = new UserInfo(u2_id, u2_fn, u2_ln);
                    mp = new MatchPair(u1, u1_yr, u2, u2_yr);
                }

                p_id = rst.getLong(9);
                a_id = rst.getLong(10);
                p_link = rst.getString(11);
                a_name = rst.getString(12);

                p = new PhotoInfo(p_id, a_id, p_link, a_name);
                mp.addSharedPhoto(p);
                results.add(mp);

                ++counter;
            }


            stmt.executeUpdate("DROP VIEW tagged_photo_info");
            stmt.executeUpdate("DROP VIEW tagged_together");
            stmt.executeUpdate("DROP VIEW not_friends");

            rst.close();
            stmt.close(); 
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }
    
    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);

            */

                // stmt.executeUpdate("DROP VIEW all_friendships");
                // stmt.executeUpdate("DROP VIEW not_friends");
                // stmt.executeUpdate("DROP VIEW num_mutuals");

                stmt.executeUpdate(
                    " CREATE VIEW all_friendships AS " + 
                    " SELECT DISTINCT u1.User_ID AS u_id1, u2.User_ID AS u_id2 " +
                    " FROM " + UsersTable + " u1, " + UsersTable + " u2, " + FriendsTable + " f " +
                    " WHERE u1.User_ID < u2.User_ID " +
                    " AND u1.User_ID = f.User1_ID AND u2.User_ID = f.User2_ID " +
                    " GROUP BY u1.User_ID, u2.User_ID "
                    );


                stmt.executeUpdate(
                    " CREATE VIEW not_friends AS " + 
                    " SELECT DISTINCT u3.User_ID AS u1_id, u4.User_ID AS u2_id" +
                    " FROM " + UsersTable + " u3, " + UsersTable + " u4 " +
                    " WHERE u3.User_ID < u4.User_ID " +
                    " GROUP BY u3.User_ID, u4.User_ID " +
                    " MINUS " + 
                    "(SELECT u_id1, u_id2 FROM all_friendships)"
                    );


                stmt.executeUpdate(
                    " CREATE VIEW num_mutuals AS " + 
                    " SELECT DISTINCT nf.u1_id, nf.u2_id, COUNT(*) AS num_mut" +
                    " FROM not_friends nf, " + FriendsTable + " f1, " + FriendsTable + " f2 " +
                    " WHERE ((nf.u1_id = f1.User1_ID AND nf.u2_id = f2.User1_ID AND f1.User2_ID = f2.User2_ID) " +
                    " OR (nf.u1_id = f1.User2_ID AND nf.u2_id = f2.User2_ID AND f1.User1_ID = f2.User1_ID) " +
                    " OR (nf.u1_id = f1.User1_ID AND nf.u2_id = f2.User2_ID AND f1.User2_ID = f2.User1_ID) " +
                    " OR (nf.u1_id = f1.User2_ID AND nf.u2_id = f2.User1_ID AND f1.User1_ID = f2.User2_ID)) " +
                    " GROUP BY nf.u1_id, nf.u2_id " +
                    " ORDER BY num_mut"
                    );


                ResultSet rst = stmt.executeQuery(
                    " SELECT DISTINCT u5.User_ID, u5.First_Name, u5.Last_Name, u6.User_ID, u6.First_Name, u6.Last_Name, u7.User_ID, u7.First_Name, u7.Last_Name, nm.num_mut " + 
                    " FROM not_friends nf, num_mutuals nm, " + UsersTable + " u5, " + FriendsTable + " f1, " + UsersTable + " u6, " + FriendsTable + " f2, " + UsersTable + " u7 " + 
                    " WHERE u5.User_ID <> u6.User_ID " + 
                    " AND u5.User_ID = nf.u1_id " + 
                    " AND u6.User_ID = nf.u2_id " + 
                    " AND nf.u1_id = nm.u1_id " + 
                    " AND nf.u2_id = nm.u2_id " + 
                    " AND ((u5.User_ID = f1.User1_ID AND u6.User_ID = f2.User1_ID AND u7.User_ID = f1.User2_ID AND u7.User_ID = f2.User2_ID) OR (u5.User_ID = f1.User2_ID AND u6.User_ID = f2.User2_ID AND u7.User_ID = f1.User1_ID AND u7.User_ID = f2.User1_ID) OR (u5.User_ID = f1.User1_ID AND u6.User_ID = f2.User2_ID AND u7.User_ID = f1.User2_ID AND u7.User_ID = f2.User1_ID) OR (u5.User_ID = f1.User2_ID AND u6.User_ID = f2.User1_ID AND u7.User_ID = f1.User1_ID AND u7.User_ID = f2.User2_ID))" + 
                    " ORDER BY nm.num_mut DESC, u5.User_ID ASC, u6.User_ID ASC, u7.User_ID ASC "
                    );


                long counter = 0;
                long u1_id = 0;
                String u1_fn = " ";
                String u1_ln = " ";
                long u2_id = 0;
                String u2_fn = " ";
                String u2_ln = " ";
                long u3_id = 0;
                String u3_fn = " ";
                String u3_ln = " ";

                rst.next();

                u1_id = rst.getLong(1);
                u1_fn = rst.getString(2);
                u1_ln = rst.getString(3);
                u2_id = rst.getLong(4);
                u2_fn = rst.getString(5);
                u2_ln = rst.getString(6);
                u3_id = rst.getLong(7);
                u3_fn = rst.getString(8);
                u3_ln = rst.getString(9);


                UserInfo u1 = new UserInfo(u1_id, u1_fn, u1_ln);
                UserInfo u2 = new UserInfo(u2_id, u2_fn, u2_ln);
                UserInfo u3 = new UserInfo(u3_id, u3_fn, u3_ln);
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);

                while (rst.next() && counter < num){
                    long temp_u1id = rst.getLong(1);
                    long temp_u2id = rst.getLong(4);

                    if (temp_u1id != u1_id || temp_u2id != u2_id){
                        results.add(up);
                        ++counter;
                        u1_id = temp_u1id;
                        u2_id = temp_u2id;

                        u1_fn = rst.getString(2);
                        u1_ln = rst.getString(3);
            
                        u2_fn = rst.getString(5);
                        u2_ln = rst.getString(6);

                        u1 = new UserInfo(u1_id, u1_fn, u1_ln);
                        u2 = new UserInfo(u2_id, u2_fn, u2_ln);
                        up = new UsersPair(u1, u2);

                        u3_id = rst.getLong(7);
                        u3_fn = rst.getString(8);
                        u3_ln = rst.getString(9);
                        u3 = new UserInfo(u3_id, u3_fn, u3_ln);
                        up.addSharedFriend(u3);
                    }
                    
                    else
                    {
                        u3_id = rst.getLong(7);
                        u3_fn = rst.getString(8);
                        u3_ln = rst.getString(9);
                        u3 = new UserInfo(u3_id, u3_fn, u3_ln);
                        up.addSharedFriend(u3);
                        
                    }
            }

            stmt.executeUpdate("DROP VIEW not_friends");
            stmt.executeUpdate("DROP VIEW all_friendships");
            stmt.executeUpdate("DROP VIEW num_mutuals");

            stmt.close();
            rst.close();
        
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        // rst.close();
        // stmt.close();

        return results;
    }
    
    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
                
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT State_Name, Count(*) FROM " + CitiesTable + ", " + EventsTable +
                " WHERE Event_City_ID = City_ID GROUP BY State_Name HAVING COUNT(*) =" +
                " (SELECT MAX(COUNT(*)) FROM " + CitiesTable + ", " + EventsTable + 
                " WHERE Event_City_ID = City_ID GROUP BY State_Name)" + 
                " ORDER BY State_Name");
            
            rst.next();
            long temp = rst.getLong(2);
            EventStateInfo info = new EventStateInfo(temp);
            info.addState(rst.getString(1));
            
            while(rst.next()){
                info.addState(rst.getString(1));
            }
            rst.close();
            stmt.close();   

            return info;   
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }
    
    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */

            long first_id = 0;
            String first_fn = "";
            String first_ln = "";

            long last_id = 0;
            String last_fn = "";
            String last_ln = "";

            ResultSet rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name FROM " + UsersTable +
                " JOIN (" + 
                " SELECT DISTINCT User1_ID, User2_ID FROM " + FriendsTable + 
                " WHERE User1_ID = '215' OR User2_ID = '215'" + 
                " ) " + 
                " friends_of_user ON (User_ID = User2_ID OR User_ID = User1_ID)" + 
                " ORDER BY YEAR_OF_BIRTH DESC, User_ID DESC");

            while(rst.next()){
                if (rst.isFirst()){
                    first_id = rst.getLong(1);
                    first_fn = rst.getString(2);
                    first_ln = rst.getString(3);
                }
                if (rst.isLast()){
                    last_id = rst.getLong(1);
                    last_fn = rst.getString(2);
                    last_ln = rst.getString(3);
                }
            }
            
            rst.close();
            stmt.close(); 

            return new AgeInfo(new UserInfo(last_id, last_fn, last_ln), new UserInfo(first_id, first_fn, first_ln));                // placeholder for compilation
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }
    
    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);

            */

            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT u1.User_ID, u1.First_Name, u1.Last_Name, u2.User_ID, u2.First_Name, u2.Last_Name " +
                " FROM " + UsersTable + " u1" +
                " JOIN " + FriendsTable + " f ON u1.User_ID = f.User1_ID" +
                " JOIN " + UsersTable + " u2 ON u2.User_ID = f.User2_ID" +
                " WHERE u1.User_ID <> u2.User_ID AND (u1.Year_OF_BIRTH - u2.Year_OF_BIRTH < 10 AND u2.Year_OF_BIRTH - u1.Year_OF_BIRTH < 10)" + 
                " INTERSECT " +
                " (SELECT DISTINCT u3.User_ID, u3.First_Name, u3.Last_Name, u4.User_ID, u4.First_Name, u4.Last_Name FROM " + UsersTable + " u3, " + UsersTable + " u4, " + HometownCitiesTable + " h1, " + HometownCitiesTable + " h2" +
                " WHERE u3.User_ID = h1.User_ID AND u4.User_ID = h2.User_ID " + 
                " AND h1.Hometown_City_ID = h2.Hometown_City_ID AND u3.User_ID <> u4.User_ID) " + 
                " INTERSECT " + 
                " (SELECT u5.User_ID, u5.First_Name, u5.Last_Name, u6.User_ID, u6.First_Name, u6.Last_Name FROM " + UsersTable + " u5, " + UsersTable + " u6 WHERE u5.Last_Name = u6.Last_Name AND u5.User_ID <> u6.User_ID)");

            long s1_id = 0;
            String s1_fn = "";
            String s1_ln = "";

            long s2_id = 0;
            String s2_fn = "";
            String s2_ln = "";
            
            while(rst.next()){
                s1_id = rst.getLong(1);
                s1_fn = rst.getString(2);
                s1_ln = rst.getString(3);

                s2_id = rst.getLong(4);
                s2_fn = rst.getString(5);
                s2_ln = rst.getString(6);

                UserInfo u1 = new UserInfo(s1_id, s1_fn, s1_ln);
                UserInfo u2 = new UserInfo(s2_id, s2_fn, s2_ln);

                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            }

              rst.close();
              stmt.close(); 

        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
