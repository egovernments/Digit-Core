import React from "react";
import { EditAttributes } from "./EditAttributes";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EditAttributes",
  component: EditAttributes,
};

export const Default = () => <EditAttributes />;
export const Fill = () => <EditAttributes fill="blue" />;
export const Size = () => <EditAttributes height="50" width="50" />;
export const CustomStyle = () => <EditAttributes style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EditAttributes className="custom-class" />;

export const Clickable = () => <EditAttributes onClick={()=>console.log("clicked")} />;

const Template = (args) => <EditAttributes {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
