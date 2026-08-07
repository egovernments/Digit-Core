import React from "react";
import { NorthEast } from "./NorthEast";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NorthEast",
  component: NorthEast,
};

export const Default = () => <NorthEast />;
export const Fill = () => <NorthEast fill="blue" />;
export const Size = () => <NorthEast height="50" width="50" />;
export const CustomStyle = () => <NorthEast style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NorthEast className="custom-class" />;

export const Clickable = () => <NorthEast onClick={()=>console.log("clicked")} />;

const Template = (args) => <NorthEast {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
