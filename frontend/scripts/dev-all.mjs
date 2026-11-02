/*
 * npm run dev:all  →  starts the Spring Boot backend AND the React dev server together.
 * Press Ctrl+C once to stop both. (MySQL must already be running.)
 * You can still run them separately: backend from IntelliJ, frontend with `npm run dev`.
 */
import { spawn } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const projectDir = path.resolve(frontendDir, '..');
const isWindows = process.platform === 'win32';

const run = (name, color, command, args, cwd) => {
  const child = spawn(command, args, { cwd, shell: true, env: process.env });
  const prefix = `\x1b[${color}m[${name}]\x1b[0m `;
  const pipe = (stream, out) => stream.on('data', (d) => d.toString().split(/\r?\n/).filter(Boolean).forEach((l) => out.write(prefix + l + '\n')));
  pipe(child.stdout, process.stdout);
  pipe(child.stderr, process.stderr);
  child.on('exit', (code) => console.log(`${prefix}stopped (exit code ${code})`));
  return child;
};

const backend = run('backend', 36, isWindows ? 'mvnw.cmd' : './mvnw', ['spring-boot:run'], projectDir);
const frontend = run('frontend', 35, 'npm', ['run', 'dev'], frontendDir);

const stop = () => { backend.kill(); frontend.kill(); process.exit(0); };
process.on('SIGINT', stop);
process.on('SIGTERM', stop);
