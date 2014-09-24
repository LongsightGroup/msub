update rwikihistory set rwikihistory.name = replace(lower(rwikihistory.name), '.', '/');
update rwikihistory set rwikihistory.referenced = replace(lower(rwikihistory.referenced), '.', '/');
update rwikiobject set rwikiobject.name = replace(lower(rwikiobject.name), '.', '/');
update rwikiobject set rwikiobject.referenced = replace(lower(rwikiobject.referenced), '.', '/');
print 'You may want to check the content of each object for subspace links'
print 'You may want to check the content of each object for subspace links';
