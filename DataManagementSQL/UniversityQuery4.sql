--Justin Meisner
--484 HW2
--UniversityQuery4.sql

CREATE VIEW StudentPairs AS 
SELECT DISTINCT student1.SID AS SID1, student2.SID AS SID2
FROM Students student1, Students student2, Enrollments e1, Enrollments e2
WHERE student1.SID < student2.SID AND e1.SID = student1.SID AND e2.SID = student2.SID AND e1.CID = e2.CID 
AND NOT EXISTS
(
	SELECT constr_check.m1SID, constr_check.m2SID
	FROM 
	(
		SELECT m1.SID AS m1SID, m2.SID AS m2SID
		FROM Members m1, Members m2
		WHERE m1.PID = m2.PID
	) constr_check
	WHERE constr_check.m1SID = student1.SID AND constr_check.m2SID = student2.SID
)
;
