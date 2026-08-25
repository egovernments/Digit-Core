import React from "react";
import { TorchNoun } from "./TorchNoun";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TorchNoun",
  component: TorchNoun,
};

export const Default = () => <TorchNoun />;
export const Fill = () => <TorchNoun fill="blue" />;
export const Size = () => <TorchNoun height="50" width="50" />;
export const CustomStyle = () => <TorchNoun style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TorchNoun className="custom-class" />;
export const Clickable = () => <TorchNoun onClick={()=>console.log("clicked")} />;

const Template = (args) => <TorchNoun {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
