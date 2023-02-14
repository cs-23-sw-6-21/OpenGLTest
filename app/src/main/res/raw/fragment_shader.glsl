precision mediump float;
uniform float time;
uniform vec2 resolution;
void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = vec4(uv, (1.0 + sin(time)) / 2.0, 1.0);
}