# Defines the possible values for the ack type in STOMP `ACKNOWLEDGEMENT`.
#
# `auto`: ACK type auto
# `client`: ACK type client
# `client-individual`: ACK type client-individual
public type AckType "auto"|"client"|"client-individual";

# Constant for STOMP ack type auto
public const AUTO = "auto";

# Constant for STOMP ack type client
public const CLIENT = "client";

# Constant for STOMP ack type client-individual
public const CLIENTINDIVIDUAL = "client-individual";

public const ENDOFLINE = "\n0000";

