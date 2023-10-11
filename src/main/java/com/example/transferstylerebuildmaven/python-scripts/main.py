import argparse
import numpy as np
import os
import torch

from style_transfer_v2 import vgg19_slice_model_gatys, vgg19_pytorch_model_gatys


def save_location_1(config):
    content_img_path = os.path.join(config['location_input_folder'], config['content_img_name'])
    style_img_path_1 = os.path.join(config['location_input_folder'], config['style_img_name'])

    out_dir_name = 'combined_' + os.path.split(content_img_path)[1].split('.')[0] + '_' + \
                   os.path.split(style_img_path_1)[1].split('.')[0] + '_'

#     output_path = os.path.join(config['location_output_folder'], out_dir_name)
    output_path = config['location_output_folder']
    os.makedirs(output_path, exist_ok=True)

    if not os.path.exists(content_img_path):
        raise FileNotFoundError("Content image is missing")
    if not os.path.exists(style_img_path_1):
        FileNotFoundError("Style image 1 is missing")

    return content_img_path, style_img_path_1, output_path


def neural_style_transfer_gatys_1_styles(config):
    content_img_path, style_img_path_1, output_path = save_location_1(config)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    if config['optimizer'] == 'adam':
        vgg19_slice_model_gatys. \
            style_transfer_gatys_one_style_adam(content_img_path, style_img_path_1, output_path, device,
                                                config['height'], style_weight=config['style_weight'])
    else:
         vgg19_pytorch_model_gatys. \
            style_transfer_gatys_one_style_lbfgs(content_img_path, style_img_path_1, output_path, device,
                                                 config['height'], config['style_weight'])


if __name__ == "__main__":

    parser = argparse.ArgumentParser()
    parser.add_argument("--location_input_folder", type=str,
                        default='C:\\Users\\user\\IdeaProjects\\TransferStyle\\src\\main\\resources\\styleTransferGatysInput\\test')
    parser.add_argument("--location_output_folder", type=str,
                        default='C:\\Users\\user\\IdeaProjects\\TransferStyle\\src\\main\\resources\\styleTransferGatysOutput\\test')

    parser.add_argument("--content_img_name", type=str, help="content image name", default='lion.jpg')
    parser.add_argument("--style_img_name", type=str, help="style image name", default='mosaic.jpg')
    parser.add_argument('--severalStyleImages', type=bool, default=False)
    parser.add_argument('--style_img_name_2', type=str, default='mosaic.jpg')

    parser.add_argument("--height", type=int, help="height of content and style images", default=512)

    parser.add_argument("--content_weight", type=float, help="weight factor for content loss", default=1e5)
    parser.add_argument("--style_weight", type=float, help="weight factor for style loss", default=2)

    parser.add_argument("--optimizer", type=str, choices=['lbfgs', 'adam'], default='lbfgs')

    parser.add_argument("--init_method", type=str, choices=['noise_gaus', 'noise_white', 'content', 'style'],
                        default='content')

    parser.add_argument('--build_graph', type=bool, default=False)

    args = parser.parse_args()

    print(args)

    default_resource_dir = os.path.join(os.path.dirname(__file__), 'data')
    content_images_dir = os.path.join(default_resource_dir, 'content-images')
    style_images_dir = os.path.join(default_resource_dir, 'style-images')
    output_img_dir = os.path.join(default_resource_dir, 'output-images')

    # just wrapping settings into a dictionary
    optimization_config = dict()
    for arg in vars(args):
        optimization_config[arg] = getattr(args, arg)
    optimization_config['content_images_dir'] = content_images_dir
    optimization_config['style_images_dir'] = style_images_dir
    optimization_config['output_img_dir'] = output_img_dir

    neural_style_transfer_gatys_1_styles(optimization_config)
