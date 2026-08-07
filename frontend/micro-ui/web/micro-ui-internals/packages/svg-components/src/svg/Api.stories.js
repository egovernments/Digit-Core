import React from "react";
import { Api } from "./Api";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Api",
  component: Api,
};

export const Default = () => <Api />;
export const Fill = () => <Api fill="blue" />;
export const Size = () => <Api height="50" width="50" />;
export const CustomStyle = () => <Api style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Api className="custom-class" />;
export const Clickable = () => <Api onClick={()=>console.log("clicked")} />;

const Template = (args) => <Api {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
