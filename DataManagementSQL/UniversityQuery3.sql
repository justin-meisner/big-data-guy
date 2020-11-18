--Justin Meisner
--484 HW2
--UniversityQuery3.sql

SELECT student.SID
FROM Students student
WHERE student.SID IN
(
	SELECT temp_s.SID 
	FROM Students temp_s, Enrollments e1, Enrollments e2, Enrollments e3, Courses c1, Courses c2, Courses c3
	WHERE
	(	
		(temp_s.SID = e1.SID AND e1.CID = c1.CID AND c1.C_Name = 'EECS442') AND 
		(temp_s.SID = e2.SID AND e2.CID = c2.CID AND c2.C_Name = 'EECS445') AND
		(temp_s.SID = e3.SID AND e3.CID = c3.CID AND c3.C_Name = 'EECS492')
	)
	UNION
	SELECT temp_s.SID 
	FROM Students temp_s, Enrollments e1, Enrollments e2, Courses c1, Courses c2
	WHERE
	(
		(temp_s.SID = e1.SID AND e1.CID = c1.CID AND c1.C_Name = 'EECS482') AND 
		(temp_s.SID = e2.SID AND e2.CID = c2.CID AND c2.C_Name = 'EECS486')
	)
	UNION
	SELECT temp_s.SID
	FROM Students temp_s, Enrollments e, Courses c
	WHERE
	(
		temp_s.SID = e.SID AND e.CID = c.CID AND c.C_Name = 'EECS281'
	)
)
ORDER BY student.SID ASC
;