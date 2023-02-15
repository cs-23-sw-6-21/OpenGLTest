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
    vec2 uv = 1.0 - (gl_FragCoord.yx / resolution.yx);
    vec4 img = texture2D(cam, uv);

    vec4 n[9];
    make_kernel(n, cam, uv);

    vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
    vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
    vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));

    if (sobel.r < 0.1 && sobel.g < 0.1 && sobel.b < 0.1) {
        sobel = 1.0 - sobel;
    }
    else {
        sobel = img ;
    }

    if (uv.y < 0.5) {
        sobel = img;
    }

    gl_FragColor = vec4(sobel.rgb, 1.0);
}