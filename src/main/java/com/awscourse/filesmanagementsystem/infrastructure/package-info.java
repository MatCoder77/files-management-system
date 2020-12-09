@org.hibernate.annotations.GenericGenerator(
        name = ID_GENERATOR,
        strategy = "enhanced-sequence",
        parameters = {
                @org.hibernate.annotations.Parameter(name = "sequence_name", value = "GENERAL_SEQUENCE"),
                @org.hibernate.annotations.Parameter(name = "initial_value", value = "1000")
        }
)
package com.awscourse.filesmanagementsystem.infrastructure;

import static com.awscourse.filesmanagementsystem.infrastructure.jpa.PersistenceConstants.ID_GENERATOR;
