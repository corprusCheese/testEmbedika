CREATE TABLE cars
(
    id SERIAL PRIMARY KEY,
    number   VARCHAR UNIQUE NOT NULL,
    stamp    VARCHAR NOT NULL,
    color    VARCHAR NOT NULL,
    year_of_release  INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT(now() AT TIME ZONE 'utc') NOT NULL
);

CREATE TABLE requests
(
   id SERIAL PRIMARY KEY,
   url VARCHAR NOT NULL,
   start_processing_at TIMESTAMP NOT NULL,
   end_processing_at TIMESTAMP NOT NULL
);
