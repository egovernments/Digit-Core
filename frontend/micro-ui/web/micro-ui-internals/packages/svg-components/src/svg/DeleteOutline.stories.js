import React from "react";
import { DeleteOutline } from "./DeleteOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DeleteOutline",
  component: DeleteOutline,
};

export const Default = () => <DeleteOutline />;
export const Fill = () => <DeleteOutline fill="blue" />;
export const Size = () => <DeleteOutline height="50" width="50" />;
export const CustomStyle = () => <DeleteOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DeleteOutline className="custom-class" />;

export const Clickable = () => <DeleteOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <DeleteOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
