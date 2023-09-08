import React from "react";
import { Badge } from "./Badge";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Badge",
  component: Badge,
};

export const Default = () => <Badge />;
export const Fill = () => <Badge fill="blue" />;
export const Size = () => <Badge height="50" width="50" />;
export const CustomStyle = () => <Badge style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Badge className="custom-class" />;

export const Clickable = () => <Badge onClick={()=>console.log("clicked")} />;

const Template = (args) => <Badge {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
