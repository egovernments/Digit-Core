import React from "react";
import { DirectionsSubway } from "./DirectionsSubway";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsSubway",
  component: DirectionsSubway,
};

export const Default = () => <DirectionsSubway />;
export const Fill = () => <DirectionsSubway fill="blue" />;
export const Size = () => <DirectionsSubway height="50" width="50" />;
export const CustomStyle = () => <DirectionsSubway style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsSubway className="custom-class" />;

export const Clickable = () => <DirectionsSubway onClick={()=>console.log("clicked")} />;

const Template = (args) => <DirectionsSubway {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
