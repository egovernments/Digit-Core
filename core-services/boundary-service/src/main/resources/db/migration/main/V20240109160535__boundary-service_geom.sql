-- Add a new column named 'geom' of type 'geometry'
ALTER TABLE boundary
ADD COLUMN geom GEOMETRY;
