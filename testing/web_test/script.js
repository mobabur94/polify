// noprotect

;(function($, window, document, undefined){

  var file_input;
  var complexity_input;
  var process_button;

  var canvas;
  var context;
  var image;

  $(function(){
    file_input = $('#file');
    complexity_input = $('#complexity');
    process_button = $('#process');

    canvas = $("#canvas")[0];
    context = canvas.getContext('2d');

    file_input.on('change', read_file);

    process_button.on('click', re_process);
  });

  function read_file(){
    if ((!this.disabled) && this.files && this.files[0]) {
      var reader = new FileReader();
      image = new Image();

      reader.readAsDataURL(this.files[0]);
      reader.onload = function(f){
        image.src = f.target.result;
        image.onerror= function() {
          alert('invalid file type: '+ file.type);
        };
      };
    }
  }

  function re_process(){
    if((!file_input[0].disabled) && file_input[0].files && file_input[0].files[0]){
      process_image(image);
    }
  }

  function process_image(image){
    var controls = get_controls();

    var temp_canvas = $("<canvas />")[0];
    var temp_context = temp_canvas.getContext('2d');

    clear_canvas(canvas, context);
    clear_canvas(temp_canvas, temp_context);

    resize_canvas(canvas, image);
    resize_canvas(temp_canvas, image);

    temp_context.drawImage(image, 0, 0);

    var image_data = temp_context.getImageData(0, 0, temp_canvas.width, temp_canvas.height);
    var image_copy = temp_context.getImageData(0, 0, temp_canvas.width, temp_canvas.height);

    var blur_data = blur(image_data, controls.blur);
    var gray_data = grayscale(blur_data);
    var edge_data = edges(blur_data);

    var edge_points = edgepoints(edge_data);
    var more_points = randompoints(edge_points, controls.pointcount, temp_canvas.width, temp_canvas.height);

    var polygons = triangles(more_points);
    var result = colorize(polygons, image_copy);

    draw_result(image_copy, canvas, context, result);
  }

  function clear_canvas(c, x){
    x.clearRect(x, 0, 0, c.width, c.height);
  }

  function resize_canvas(c, i){
    c.width = i.width;
    c.height = i.height;

    c.style.width = c.width + 'px';
    c.style.height = c.height + 'px';
  }

  function get_controls(){
    var c = {};
    var complexity = complexity_input.val();

    c.blur = scale_control(6 - complexity, 1, 5, 1, 10);
    c.pointcount = scale_control(complexity, 1, 5, 5000, 50000);

    return c;
  }

  function scale_control(v, ilo, ihi, olo, ohi){
    return parseInt((((v - ilo) / (ihi - ilo)) * (ohi - olo)) + olo, 10);
  }

  function blur(image_data, radius){
    var mul_table = [ 1,57,41,21,203,34,97,73,227,91,149,62,105,45,39,137,241,107,3,173,39,71,65,238,219,101,187,87,81,151,141,133,249,117,221,209,197,187,177,169,5,153,73,139,133,127,243,233,223,107,103,99,191,23,177,171,165,159,77,149,9,139,135,131,253,245,119,231,224,109,211,103,25,195,189,23,45,175,171,83,81,79,155,151,147,9,141,137,67,131,129,251,123,30,235,115,113,221,217,53,13,51,50,49,193,189,185,91,179,175,43,169,83,163,5,79,155,19,75,147,145,143,35,69,17,67,33,65,255,251,247,243,239,59,29,229,113,111,219,27,213,105,207,51,201,199,49,193,191,47,93,183,181,179,11,87,43,85,167,165,163,161,159,157,155,77,19,75,37,73,145,143,141,35,138,137,135,67,33,131,129,255,63,250,247,61,121,239,237,117,29,229,227,225,111,55,109,216,213,211,209,207,205,203,201,199,197,195,193,48,190,47,93,185,183,181,179,178,176,175,173,171,85,21,167,165,41,163,161,5,79,157,78,154,153,19,75,149,74,147,73,144,143,71,141,140,139,137,17,135,134,133,66,131,65,129,1];
    var shg_table = [0,9,10,10,14,12,14,14,16,15,16,15,16,15,15,17,18,17,12,18,16,17,17,19,19,18,19,18,18,19,19,19,20,19,20,20,20,20,20,20,15,20,19,20,20,20,21,21,21,20,20,20,21,18,21,21,21,21,20,21,17,21,21,21,22,22,21,22,22,21,22,21,19,22,22,19,20,22,22,21,21,21,22,22,22,18,22,22,21,22,22,23,22,20,23,22,22,23,23,21,19,21,21,21,23,23,23,22,23,23,21,23,22,23,18,22,23,20,22,23,23,23,21,22,20,22,21,22,24,24,24,24,24,22,21,24,23,23,24,21,24,23,24,22,24,24,22,24,24,22,23,24,24,24,20,23,22,23,24,24,24,24,24,24,24,23,21,23,22,23,24,24,24,22,24,24,24,23,22,24,24,25,23,25,25,23,24,25,25,24,22,25,25,25,24,23,24,25,25,25,25,25,25,25,25,25,25,25,25,23,25,23,24,25,25,25,25,25,25,25,25,25,24,22,25,25,23,25,25,20,24,25,24,25,25,22,24,25,24,25,24,25,25,24,25,25,25,25,22,25,25,25,24,25,24,25,18];
    function boxBlurCanvas(image_data, radius){
      if(isNaN(radius) || radius < 1){
        return image_data;
      }
      radius |= 0;
      var pixels = image_data.data;
      var width = image_data.width;
      var height = image_data.height;
      var rsum, gsum, bsum, asum, x, y, i, p, p1, p2, yp, yi, yw, idx;
      var wm = width - 1;
      var hm = height - 1;
      var wh = width * height;
      var rad1 = radius + 1;
      var r = [];
      var g = [];
      var b = [];
      var mul_sum = mul_table[radius];
      var shg_sum = shg_table[radius];
      var vmin = [];
      var vmax = [];
      yw = yi = 0;
      for(y = 0; y < height; y++){
        rsum = pixels[yw] * rad1;
        gsum = pixels[yw + 1] * rad1;
        bsum = pixels[yw + 2] * rad1;
        for(i = 1; i <= radius; i++){
          p = yw + (((i > wm ? wm : i)) << 2);
          rsum += pixels[p++];
          gsum += pixels[p++];
          bsum += pixels[p++];
        }
        for(x = 0; x < width; x++){
          r[yi] = rsum;
          g[yi] = gsum;
          b[yi] = bsum;
          if(y === 0) {
            vmin[x] = ((p = x + rad1) < wm ? p : wm) << 2;
            vmax[x] = ((p = x - radius) > 0 ? p << 2 : 0);
          }
          p1 = yw + vmin[x];
          p2 = yw + vmax[x];
          rsum += pixels[p1++] - pixels[p2++];
          gsum += pixels[p1++] - pixels[p2++];
          bsum += pixels[p1++] - pixels[p2++];
          yi++;
        }
        yw += (width << 2);
      }
      for(x = 0; x < width; x++){
        yp = x;
        rsum = r[yp] * rad1;
        gsum = g[yp] * rad1;
        bsum = b[yp] * rad1;
        for(i = 1; i <= radius; i++){
          yp += (i > hm ? 0 : width);
          rsum += r[yp];
          gsum += g[yp];
          bsum += b[yp];
        }
        yi = x << 2;
        for(y = 0; y < height; y++){
          pixels[yi] = (rsum * mul_sum) >>> shg_sum;
          pixels[yi + 1] = (gsum * mul_sum) >>> shg_sum;
          pixels[yi + 2] = (bsum * mul_sum) >>> shg_sum;
          if(x === 0){
            vmin[y] = ((p = y + rad1) < hm ? p : hm) * width;
            vmax[y] = ((p = y - radius) > 0 ? p * width : 0);
          }
          p1 = x + vmin[y];
          p2 = x + vmax[y];
          rsum += r[p1] - r[p2];
          gsum += g[p1] - g[p2];
          bsum += b[p1] - b[p2];
          yi += width << 2;
        }
      }
      return image_data;
    }
    return boxBlurCanvas(image_data, radius);
  }

  function grayscale(image_data){
    for (var i = 0; i < image_data.data.length; i+=4){
      var brightness = 0 +
          (0.299 * image_data.data[i + 0]) +
          (0.587 * image_data.data[i + 1]) +
          (0.114 * image_data.data[i + 2]);

      image_data.data[i + 0] = brightness;
      image_data.data[i + 1] = brightness;
      image_data.data[i + 2] = brightness;
    }
    return image_data;
  }

  function edges(image_data){
    function detectEdges(image_data){
      var matrix = getEdgeMatrix(5).slice();
      var multiplier = 1;
      var k, len;
      var data = image_data.data;
      len = data.length >> 2;
      var copy = new Uint8Array(len);
      for(i = 0; i < len; i++){
        copy[i] = data[i << 2];
      }
      var width  = image_data.width | 0;
      var height = image_data.height | 0;
      var size  = Math.sqrt(matrix.length);
      var range = size * 0.5 | 0;
      var x, y;
      var r, g, b, v;
      var col, row, sx, sy;
      var i, istep, jstep, kstep;
      for(y = 0; y < height; y += multiplier){
        istep = y * width;
        for(x = 0; x < width; x += multiplier){
          r = g = b = 0;
          for(row = -range; row <= range; row++){
            sy = y + row;
            jstep = sy * width;
            kstep = (row + range) * size;
            if(sy >= 0 && sy < height){
              for(col = -range; col <= range; col++){
                sx = x + col;
                if(sx >= 0 && sx < width && ( v = matrix[( col + range ) + kstep] )){
                  r += copy[sx + jstep] * v;
                }
              }
            }
          }
          if(r < 0){
            r = 0;
          }else if( r > 255){
            r = 255;
          }
          data[( x + istep ) << 2] = r & 0xFF;
        }
      }
      return image_data;
    }
    function getEdgeMatrix(size){
      var matrix = [];
      var side = size * 2 + 1;
      var i, len = side * side;
      var center = len * 0.5 | 0;
      for(i = 0; i < len; i++){
        matrix[i] = i === center ? -len + 1 : 1;
      }
      return matrix;
    }
    return detectEdges(image_data);
  }

  function edgepoints(image_data){
    var multiplier = 1;
    var edge_detect_value = 50;
    var width  = image_data.width;
    var height = image_data.height;
    var data = image_data.data;
    var points = [];
    var x, y, row, col, sx, sy, step, sum, total;
    for(y = 0; y < height; y += multiplier){
      for(x = 0; x < width; x += multiplier){
        sum = total = 0;
        for(row = -1; row <= 1; row++){
          sy = y + row;
          step = sy * width;
          if(sy >= 0 && sy < height){
            for(col = -1; col <= 1; col++){
              sx = x + col;
              if(sx >= 0 && sx < width){
                sum += data[(sx + step) << 2];
                total++;
              }
            }
          }
        }
        if(total){
          sum /= total;
        }
        if(sum > edge_detect_value){
          points.push({x: x, y: y});
        }
      }
    }
    return points;
  }

  function randompoints(points, limit, width, height) {
    var j;
    var result = [];
    var i = 0;
    var i_len = points.length;
    var t_len = i_len;
    points = points.slice();
    while(i < limit && i < i_len){
      j = t_len * Math.random() | 0;
      result.push({
        x: points[j].x,
        y: points[j].y
      });
      t_len--;
      i++;
    }
    var x, y;
    for(x = 0; x < width; x += 100){
      result.push({
        x: ~~x,
        y: 0
      });
      result.push({
        x: ~~x,
        y: height
      });
    }
    for(y = 0; y < height; y += 100){
      result.push({
        x: 0,
        y: ~~y
      });
      result.push({
        x: width,
        y: ~~y
      });
    }
    result.push({
      x: 0,
      y: height
    });
    result.push({
      x: width,
      y: height
    });
    return result;
  }

  function triangles(points){
    function Triangle(a, b, c){
      this.a = a;
      this.b = b;
      this.c = c;
      var A = b.x - a.x,
          B = b.y - a.y,
          C = c.x - a.x,
          D = c.y - a.y,
          E = A * (a.x + b.x) + B * (a.y + b.y),
          F = C * (a.x + c.x) + D * (a.y + c.y),
          G = 2 * (A * (c.y - b.y) - B * (c.x - b.x)),
          minx, miny, dx, dy;
      if(Math.abs(G) < 1){
        minx = Math.min(a.x, b.x, c.x);
        miny = Math.min(a.y, b.y, c.y);
        dx = (Math.max(a.x, b.x, c.x) - minx) * 0.5;
        dy = (Math.max(a.y, b.y, c.y) - miny) * 0.5;
        this.x = minx + dx;
        this.y = miny + dy;
        this.r = dx * dx + dy * dy;
      }else{
        this.x = (D * E - B * F) / G;
        this.y = (A * F - C * E) / G;
        dx = this.x - a.x;
        dy = this.y - a.y;
        this.r = dx * dx + dy * dy;
      }
    }
    Triangle.prototype.draw = function(ctx){
      ctx.beginPath();
      ctx.moveTo(this.a.x, this.a.y);
      ctx.lineTo(this.b.x, this.b.y);
      ctx.lineTo(this.c.x, this.c.y);
      ctx.closePath();
      ctx.stroke();
    };
    function byX(a, b){
      return b.x - a.x;
    }
    function dedup(edges){
      var j = edges.length,
          a, b, i, m, n;
      outer: while(j){
        b = edges[--j];
        a = edges[--j];
        i = j;
        while(i){
          n = edges[--i];
          m = edges[--i];
          if((a === m && b === n) || (a === n && b === m)){
            edges.splice(j, 2);
            edges.splice(i, 2);
            j -= 2;
            continue outer;
          }
        }
      }
    }
    function triangulate(vertices){
      if(vertices.length < 3) return [];
      vertices.sort(byX);
      var i = vertices.length - 1,
          xmin = vertices[i].x,
          xmax = vertices[0].x,
          ymin = vertices[i].y,
          ymax = ymin;
      while(i--){
        if(vertices[i].y < ymin) ymin = vertices[i].y;
        if(vertices[i].y > ymax) ymax = vertices[i].y;
      }
      var dx = xmax - xmin,
          dy = ymax - ymin,
          dmax = (dx > dy) ? dx : dy,
          xmid = (xmax + xmin) * 0.5,
          ymid = (ymax + ymin) * 0.5,
          open = [
            new Triangle({
              x: xmid - 20 * dmax,
              y: ymid - dmax,
              __sentinel: true
            }, {
              x: xmid,
              y: ymid + 20 * dmax,
              __sentinel: true
            }, {
              x: xmid + 20 * dmax,
              y: ymid - dmax,
              __sentinel: true
            })
          ],
          closed = [],
          edges = [],
          j, a, b;
      i = vertices.length;
      while(i--){
        edges.length = 0;
        j = open.length;
        while(j--){
          dx = vertices[i].x - open[j].x;
          if(dx > 0 && dx * dx > open[j].r){
            closed.push(open[j]);
            open.splice(j, 1);
            continue;
          }
          dy = vertices[i].y - open[j].y;
          if(dx * dx + dy * dy > open[j].r){
            continue;
          }
          edges.push(open[j].a, open[j].b, open[j].b, open[j].c, open[j].c, open[j].a);
          open.splice(j, 1);
        }
        dedup(edges);
        j = edges.length;
        while(j){
          b = edges[--j];
          a = edges[--j];
          open.push(new Triangle(a, b, vertices[i]));
        }
      }
      Array.prototype.push.apply(closed, open);
      i = closed.length;
      while(i--){
        if(closed[i].a.__sentinel || closed[i].b.__sentinel || closed[i].c.__sentinel){
          closed.splice(i, 1);
        }
      }
      return closed;
    }
    return triangulate(points);
  }

  function colorize(triangles, colors){
    for(var i = 0; i < triangles.length; i++){
      t = triangles[i];
      cx = ( t.a.x + t.b.x + t.c.x ) / 3;
      cy = ( t.a.y + t.b.y + t.c.y ) / 3;
      p = ((cx | 0) + (cy | 0) * colors.width) << 2;
      t.color = 'rgb(' + colors.data[p] + ', ' + colors.data[p + 1] + ', ' + colors.data[p + 2] + ')';
    }
    return triangles;
  }

  function draw_result(background_image, canvas, context, result){
    context.rect(0, 0, canvas.width, canvas.height);
    context.fillStyle = 'black';
    context.fill();
    context.putImageData(background_image, 0, 0);
    for(var i = 0; i < result.length; i++){
      t = result[i];
      context.beginPath();
      context.moveTo(t.a.x, t.a.y);
      context.lineTo(t.b.x, t.b.y);
      context.lineTo(t.c.x, t.c.y);
      context.lineTo(t.a.x, t.a.y);
      context.fillStyle = t.color;
      context.fill();
      context.closePath();
    }
  }

})(window.jQuery, window, document);
