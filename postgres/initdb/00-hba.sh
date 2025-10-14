#!/bin/bash
set -e

#  подключения без SSL из docker bridge сети
echo "hostnossl all all 172.16.0.0/12 scram-sha-256" >> "$PGDATA/pg_hba.conf"

#  подключение с любых IP
echo "listen_addresses='*'" >> "$PGDATA/postgresql.conf"