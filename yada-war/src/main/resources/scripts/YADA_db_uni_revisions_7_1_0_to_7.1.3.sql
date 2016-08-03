UPDATE YADA_QUERY SET QUERY = 'select a.*,count(b.target) DEFAULT_PARAMS 
from yada_query a left 
join yada_param b on a.qname = b.target 
where app = ?v 
group by 
a.app,
a.qname,
a.query,
a.last_access,
a.access_count,
a.modified,
a.modified_by,
a.created,
a.created_by,
a.comments' WHERE QNAME = 'YADA queries';
UPDATE YADA_QUERY SET QUERY = 'delete from YADA_PARAM where target = ?v and name = ?v and value = ?v and rule = ?i' WHERE QNAME = 'YADA delete default param';

