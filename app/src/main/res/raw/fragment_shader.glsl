#extension GL_OES_EGL_image_external : require
precision highp float;
uniform float time;
uniform vec2 resolution;
uniform samplerExternalOES cam;
uniform vec2 camResolution;

void make_kernel(inout vec4 n[9], samplerExternalOES tex, vec2 coord) {

    float w = 1.0 / camResolution.x;
    float h = 1.0 / camResolution.y;

    n[0] = texture2D(tex, coord + vec2( -w, -h));
    n[1] = texture2D(tex, coord + vec2(0.0, -h));
    n[2] = texture2D(tex, coord + vec2(  w, -h));
    n[3] = texture2D(tex, coord + vec2( -w,0.0));
    n[4] = texture2D(tex, coord);
    n[5] = texture2D(tex, coord + vec2(  w,0.0));
    n[6] = texture2D(tex, coord + vec2( -w,  h));
    n[7] = texture2D(tex, coord + vec2(0.0,  h));
    n[8] = texture2D(tex, coord + vec2(  w,  h));
}

void main() {
    vec2 divThing = resolution.xy / camResolution.yx;
    vec2 fixedCamRes = min(divThing.x, divThing.y) * camResolution.yx;

    float halfThing = (fixedCamRes.y - resolution.y) / 2.0;

    vec2 uv = vec2(gl_FragCoord.x / fixedCamRes.x, gl_FragCoord.y / fixedCamRes.y + halfThing / resolution.y);
    vec2 camuv = 1.0 - uv.yx;


    vec4 n[9];
    make_kernel(n, cam, camuv);
    vec4 img = texture2D(cam, camuv);

    vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
    vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
    vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));

    if (sobel.r < 0.2 && sobel.g < 0.2 && sobel.b < 0.2) {
        sobel = vec4(0.0, 0.0, 0.0, sobel.a);
    }
    else {
        sobel = img;
    }

    // Make half the image just the camera
    if (gl_FragCoord.x / resolution.x < 0.5) {
        sobel = img;
    }

    // Draw outside camera with magenta
    if (uv.y <= 0.0 || uv.y >= 1.0) {
        sobel.rgb = vec3(1.0, 0.0, 1.0);
    }

    gl_FragColor = sobel;
}