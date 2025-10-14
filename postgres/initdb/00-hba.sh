#!/usr/bin/env bash
set -euo pipefail

echo "hostnossl   all   all   172.16.0.0/12   scram-sha-256" >> "$PGDATA/pg_hba.conf"

echo "listen_addresses='*'" >> "$PGDATA/postgresql.conf"
