# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:26 AS builder

# Enable corepack and install the exact pnpm version declared in package.json
RUN npm install -g corepack@latest && corepack enable && corepack prepare pnpm@10.33.2 --activate

WORKDIR /app

# Copy manifest files first to leverage layer caching
COPY package.json pnpm-lock.yaml ./

# Install dependencies (frozen lockfile for reproducibility)
RUN pnpm install --frozen-lockfile

# Copy the rest of the source
COPY . .

# Build — with ssr:false Nuxt outputs a fully static site to .output/public
RUN pnpm build

# ─── Stage 2: Serve ──────────────────────────────────────────────────────────
FROM nginx:alpine AS runner

# Remove the default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy built static files
COPY --from=builder /app/.output/public /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
