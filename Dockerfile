FROM bellsoft/liberica-openjre-debian:25

RUN addgroup --system app && adduser --system --ingroup app app

COPY /app/build/install/snowflake-mcp /app

RUN ls -laR /app

RUN mkdir -p /data && chown -R app:app /app /data

USER app

ENV SNOWFLAKE_MCP_DB_PATH=/data/snowflake-mcp.db
ENV SNOWFLAKE_MCP_HOST=0.0.0.0

EXPOSE 8080
VOLUME /data

ENTRYPOINT ["/app/bin/snowflake-mcp"]