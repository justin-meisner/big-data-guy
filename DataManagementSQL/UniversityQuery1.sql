--Justin Meisner
--484 HW2
--UniversityQuery1.sql

SELECT final_course.CID
FROM Courses final_course
WHERE final_course.CID IN
(
	SELECT temp_course.CID FROM Courses temp_course
	MINUS
	(
		SELECT enrol.CID FROM Enrollments enrol, Students stud 
		WHERE enrol.SID = stud.SID
		AND (stud.Major <> 'CS' or stud.Major IS NULL)
		GROUP BY enrol.CID
		HAVING COUNT(*) >= 10
	)
)
;