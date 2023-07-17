import React from "react";
import { Edit } from "./Edit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Edit",
  component: Edit,
};

export const Default = () => <Edit />;
export const Fill = () => <Edit fill="blue" />;
export const Size = () => <Edit height="50" width="50" />;
export const CustomStyle = () => <Edit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Edit className="custom-class" />;

export const Clickable = () => <Edit onClick={()=>console.log("clicked")} />;

const Template = (args) => <Edit {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
