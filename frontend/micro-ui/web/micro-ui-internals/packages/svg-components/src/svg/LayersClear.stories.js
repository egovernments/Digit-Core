import React from "react";
import { LayersClear } from "./LayersClear";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LayersClear",
  component: LayersClear,
};

export const Default = () => <LayersClear />;
export const Fill = () => <LayersClear fill="blue" />;
export const Size = () => <LayersClear height="50" width="50" />;
export const CustomStyle = () => <LayersClear style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LayersClear className="custom-class" />;

export const Clickable = () => <LayersClear onClick={()=>console.log("clicked")} />;

const Template = (args) => <LayersClear {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
