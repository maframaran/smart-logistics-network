# Stage 1: download and preprocess the Southeast Brazil OSM extract.
# Covers São Paulo and Rio de Janeiro states (SP→RJ corridor used in acceptance tests).
# Internet access required at build time only; the resulting image is fully self-contained.
# First build takes ~10-15 minutes due to osrm-extract processing ~380 MB of map data.
FROM osrm/osrm-backend:v5.27.1 AS builder

RUN apt-get update -qq && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*

WORKDIR /data

RUN wget -q -O region.osm.pbf \
      https://download.geofabrik.de/south-america/brazil/sudeste-latest.osm.pbf \
 && osrm-extract -p /opt/car.lua region.osm.pbf \
 && rm region.osm.pbf \
 && osrm-partition region.osrm \
 && osrm-customize region.osrm

# Stage 2: lean runtime image containing only the preprocessed graph files.
FROM osrm/osrm-backend:v5.27.1

WORKDIR /data
COPY --from=builder /data/region.osrm* ./

EXPOSE 5000
CMD ["osrm-routed", "--algorithm", "mld", "region.osrm"]
