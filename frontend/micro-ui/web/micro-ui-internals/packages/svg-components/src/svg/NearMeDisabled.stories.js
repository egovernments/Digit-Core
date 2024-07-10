import React from "react";
import { NearMeDisabled } from "./NearMeDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NearMeDisabled",
  component: NearMeDisabled,
};

export const Default = () => <NearMeDisabled />;
export const Fill = () => <NearMeDisabled fill="blue" />;
export const Size = () => <NearMeDisabled height="50" width="50" />;
export const CustomStyle = () => <NearMeDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NearMeDisabled className="custom-class" />;

export const Clickable = () => <NearMeDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <NearMeDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
