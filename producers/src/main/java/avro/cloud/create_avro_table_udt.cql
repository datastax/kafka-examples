// When using cloud there must be no keyspace created and all statements must use keyspace defined in the cloud (https://apollo.datastax.com)
DROP TABLE avro_udt_table;

CREATE type if not exists segment0_udt (
segment0_0 text,
segment0_1 text,
segment0_2 text,
segment0_3 text,
segment0_4 text,
segment0_5 text,
segment0_6 text,
segment0_7 text,
segment0_8 text,
segment0_9 text
);

CREATE type if not exists segment1_udt (
segment1_0 text,
segment1_1 text,
segment1_2 text,
segment1_3 text,
segment1_4 text,
segment1_5 text,
segment1_6 text,
segment1_7 text,
segment1_8 text,
segment1_9 text
);

CREATE type if not exists segment2_udt (
segment2_0 text,
segment2_1 text,
segment2_2 text,
segment2_3 text,
segment2_4 text,
segment2_5 text,
segment2_6 text,
segment2_7 text,
segment2_8 text,
segment2_9 text
);

CREATE type if not exists segment3_udt (
segment3_0 text,
segment3_1 text,
segment3_2 text,
segment3_3 text,
segment3_4 text,
segment3_5 text,
segment3_6 text,
segment3_7 text,
segment3_8 text,
segment3_9 text
);

CREATE type if not exists segment4_udt (
segment4_0 text,
segment4_1 text,
segment4_2 text,
segment4_3 text,
segment4_4 text,
segment4_5 text,
segment4_6 text,
segment4_7 text,
segment4_8 text,
segment4_9 text
);

CREATE type if not exists segment5_udt (
segment5_0 text,
segment5_1 text,
segment5_2 text,
segment5_3 text,
segment5_4 text,
segment5_5 text,
segment5_6 text,
segment5_7 text,
segment5_8 text,
segment5_9 text
);
CREATE type if not exists segment6_udt (
segment6_0 text,
segment6_1 text,
segment6_2 text,
segment6_3 text,
segment6_4 text,
segment6_5 text,
segment6_6 text,
segment6_7 text,
segment6_8 text,
segment6_9 text
);
CREATE type if not exists segment7_udt (
segment7_0 text,
segment7_1 text,
segment7_2 text,
segment7_3 text,
segment7_4 text,
segment7_5 text,
segment7_6 text,
segment7_7 text,
segment7_8 text,
segment7_9 text
);
CREATE type if not exists segment8_udt (
segment8_0 text,
segment8_1 text,
segment8_2 text,
segment8_3 text,
segment8_4 text,
segment8_5 text,
segment8_6 text,
segment8_7 text,
segment8_8 text,
segment8_9 text
);

CREATE type if not exists segment9_udt (
segment9_0 text,
segment9_1 text,
segment9_2 text,
segment9_3 text,
segment9_4 text,
segment9_5 text,
segment9_6 text,
segment9_7 text,
segment9_8 text,
segment9_9 text
);

create table if not exists avro_udt_table (
id int PRIMARY KEY,
udt_col0 FROZEN<segment0_udt>,
udt_col1 FROZEN<segment1_udt>,
udt_col2 FROZEN<segment2_udt>,
udt_col3 FROZEN<segment3_udt>,
udt_col4 FROZEN<segment4_udt>,
udt_col5 FROZEN<segment5_udt>,
udt_col6 FROZEN<segment6_udt>,
udt_col7 FROZEN<segment7_udt>,
udt_col8 FROZEN<segment8_udt>,
udt_col9 FROZEN<segment9_udt>
);