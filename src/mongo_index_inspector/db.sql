-- :name get-all-environments :? :*
-- :doc Get all environments
select environment_id "environment-id",
    name,
    uri,
    indexes_updated_at "indexes-updated-at"
from environment

-- :name create-environment! :<! :1
-- :doc Create an environment and return it
insert into environment (name, uri)
values (:name, :uri)
returning environment_id "environment-id",
    name,
    uri

-- :name delete-environment! :! :n
-- :doc Delete the environment with the given ID
delete from environment
where environment_id = :id

-- :name get-environment :? :1
-- :doc Get environment by ID
select environment_id "environment-id",
    name,
    uri
from environment
where environment_id = :id

-- :name update-indexes! :! :n
-- :doc Update the indexes of the environment with the given ID
update environment
set indexes = :indexes,
    indexes_updated_at = :indexes-updated-at
where environment_id = :id

-- :name get-all-indexes :? :*
-- :doc Get all indexes
select environment_id "environment-id",
    name,
    indexes
from environment