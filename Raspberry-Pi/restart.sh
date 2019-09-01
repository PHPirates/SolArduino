#!/usr/bin/env bash
# Use this file with: sudo sh ./restart.sh
sudo supervisorctl shutdown && sudo supervisord -c supervisord.conf && sudo supervisorctl update
