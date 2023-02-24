#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform samplerExternalOES camera;
uniform vec2 camera_resolution;

void main() {
    vec2 uv = 1.0 - (gl_FragCoord.yx / camera_resolution.xy);

    vec4 color = texture2D(camera, uv);

    gl_FragColor = color;
}