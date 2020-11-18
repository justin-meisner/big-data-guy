--Justin Meisner
--484 HW2
--UniversityQuery2.sql

SELECT DISTINCT stud_l.SID, stud_l.Name
FROM Students stud_l, Students stud_r, Members mem_l, Members mem_r
WHERE mem_l.SID = stud_l.SID AND mem_r.SID = stud_r.SID AND mem_r.PID = mem_l.PID AND stud_l.SID <> stud_r.SID
AND stud_r.SID IN
(
	SELECT enroll1.SID FROM Enrollments enroll1, Enrollments enroll2, Enrollments enroll3, Courses c1, Courses c2, Courses c3
	WHERE 
	(
		(enroll1.CID = c1.CID AND (c1.C_Name = 'EECS482' OR c1.C_Name = 'EECS483')) AND
		(enroll2.CID = c2.CID AND enroll2.SID = enroll1.SID AND (c2.C_Name = 'EECS484' OR c2.C_Name = 'EECS485')) AND
		(enroll3.CID = c3.CID AND enroll3.SID = enroll1.SID AND c3.C_Name = 'EECS280')
	)
)
ORDER BY stud_l.Name DESC 
;