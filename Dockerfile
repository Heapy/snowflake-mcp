FROM bellsoft/liberica-openjre-alpine:25

RUN addgroup -S app && adduser -S app -G app

COPY /app/build/install/snowflake-mcp /app

RUN ls -laR /app

RUN mkdir -p /data && chown -R app:app /app /data

USER app

ENV SNOWFLAKE_MCP_DB_PATH=/data/snowflake-mcp.db
ENV SNOWFLAKE_MCP_HOST=0.0.0.0

EXPOSE 8080
VOLUME /data

ENTRYPOINT ["/app/bin/snowflake-mcp"]